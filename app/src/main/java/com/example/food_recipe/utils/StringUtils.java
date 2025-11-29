package com.example.food_recipe.utils;

// [추가] Okt(Open Korean Text) 라이브러리 관련 클래스를 import 합니다.
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer;
import scala.collection.Seq;
import scala.collection.JavaConverters; // [추가] Scala의 Seq를 Java List로 변환하기 위해 import 합니다.
import java.util.List;
import java.util.stream.Collectors;


/**
 * 앱 전역에서 사용될 문자열 관련 유틸리티 메소드를 정의하는 클래스입니다.
 */
public class StringUtils {

    // --- [추가] 재료명 정규화를 위한 상수 정의 ---

    /**
     * [추가] 재료명에서 제거할 수식어 목록입니다.
     * '다진 마늘', '삶은 계란', '냉동 삼겹살' 등에서 핵심 재료명만 추출하기 위해 사용됩니다.
     */
    private static final List<String> MODIFIERS = List.of(
            "다진", "간", "말린", "건", "으깬", "채썬", "채 썬", "썬", "잘게썬", "잘게 썬",
            "삶은", "익힌", "볶은", "구운", "튀긴", "찐", "데친", "익은", "생", "냉동"
    );

    /**
     * [추가] 재료명에서 제거할 정도 또는 상태 표현 목록입니다.
     * '소금 약간', '후추 조금' 등에서 핵심 재료명만 추출하기 위해 사용됩니다.
     */
    private static final List<String> DEGREE_EXPRESSIONS = List.of(
            "약간", "조금", "적당량", "기호대로", "소량"
    );

    /**
     * [개선] 사용자가 입력한 재료 이름을 실무적인 수준으로 정규화합니다.
     * 사용자의 다양한 입력(예: "다진 마늘(국산) 1개")을 일관된 핵심 재료명("마늘")으로 정제합니다.
     *
     * @param input 정규화할 원본 문자열
     * @return 정규화된 문자열, 유효하지 않을 경우 빈 문자열
     */
    public static String normalizeIngredientName(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        String processed = input.trim();

        // 1단계: 괄호 안 내용 제거
        processed = processed.replaceAll("\\s*\\(.*?\\)", "")
                             .replaceAll("\\s*\\[.*?]", "")
                             .replaceAll("\\s*\\{.*?\\}", "");

        // 2단계: 수식어 및 정도 표현 제거
        for (String modifier : MODIFIERS) {
            processed = processed.replace(modifier, "");
        }
        for (String degree : DEGREE_EXPRESSIONS) {
            processed = processed.replace(degree, "");
        }

        // 3단계: 예외 패턴 처리 (자주 쓰이는 복합어 등)
        processed = processed.replaceAll("통\\s*마늘", "마늘");
        processed = processed.replaceAll("통\\s*후추", "후추");
        processed = processed.replaceAll("통\\s*깨", "깨");
        processed = processed.replaceAll("고춧가루", "고추가루"); // '고춧가루' -> '고추가루'로 표준화

        // [최종 개선] 4단계: 불필요한 특수문자 및 '숫자' 제거, 그리고 공백 정리
        // '이름'에는 숫자가 포함되어서는 안되므로, 정규식에서 허용 문자(0-9)를 제거합니다.
        processed = processed.replaceAll("[^a-zA-Z가-힣\\s]", " ");
        processed = processed.replaceAll("\\s+", " ").trim();

        return processed;
    }

    /**
     * 사용자가 입력한 검색어를 정규화합니다.
     * 현재는 재료 이름 정규화와 동일한 규칙을 적용하며, 추후 검색엔진 특화 로직을 위해 분리합니다.
     *
     * @param query 정규화할 원본 검색어
     * @return 정규화된 검색어
     */
    public static String normalizeSearchQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "";
        }
        // [추가] 검색어에 대해서도 재료명과 동일한 기본 정규화를 적용합니다.
        return query.trim().replaceAll("\\s+", " ").replaceAll("[^a-zA-Z0-9가-힣\\s]", "");
    }

    /**
     * [추가] Okt를 사용하여 입력된 텍스트에서 명사만 추출하여 정규화된 검색어를 생성합니다.
     * 불용어(예: "레시피", "방법")도 이 단계에서 제거됩니다.
     *
     * @param text 원본 텍스트
     * @return 추출된 명사들을 공백으로 연결한 문자열
     */
    public static String extractNouns(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // 1. 기본적인 정규화를 먼저 수행합니다.
        String normalizedText = normalizeSearchQuery(text);

        // 2. Okt를 사용하여 형태소 분석을 수행합니다.
        CharSequence normalizedCharSequence = normalizedText;
        Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalizedCharSequence);

        // [변경] 불안정한 문자열 처리 대신, Token 객체를 직접 사용하여 명확하게 품사를 확인합니다.
        List<KoreanTokenizer.KoreanToken> javaTokens = JavaConverters.seqAsJavaList(tokens);
        List<String> nouns = javaTokens.stream()
                .filter(token -> token.pos().toString().equals("Noun")) // 품사가 'Noun'인지 직접 확인
                .map(KoreanTokenizer.KoreanToken::text) // Token 객체에서 텍스트를 안전하게 추출
                .collect(Collectors.toList());


        // 3. 불용어 사전을 정의하고, 해당 단어들을 결과에서 제거합니다.
        List<String> stopWords = List.of("레시피", "방법", "만들기", "요리");
        nouns.removeAll(stopWords);

        // 4. 추출 및 필터링된 명사들을 공백으로 연결하여 최종 검색어를 만듭니다.
        return String.join(" ", nouns);
    }
}
