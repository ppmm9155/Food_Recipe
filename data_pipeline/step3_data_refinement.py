"""
레시피 20만 JSON → 포트폴리오용 10만 JSON 정제 스크립트
- 폴더 전체 로드
- 항상 동일한 스키마 유지 (값 없으면 "" / 숫자는 0 / 리스트는 [])
- cooking_steps.step 은 int 로 강제 변환
- 인기 60% + 카테고리 균형 40%
- 최종 5만 + 5만 분할 저장
"""

import json
from collections import Counter, defaultdict
from pathlib import Path
import random
import math
import glob
import sys

# =========================
# 사용자 설정
# =========================
INPUT_DIR = r"C:\Recipe\crawling_checkpoints"
OUTPUT_DIR = r"C:\Recipe"
RANDOM_SEED = 42
TARGET_TOTAL = 100_000
POPULAR_RATIO = 0.60
BALANCED_RATIO = 0.40

# 인기 점수 가중치
W_VIEW = 0.6
W_RECO = 0.2
W_SCRAP = 0.2

# 남길 필드
KEEP_FIELDS = {
    "RCP_SNO", "title", "imageUrl", "category_kind",
    "servings", "difficulty", "cooking_time",
    "view_count", "recommend_count", "scrap_count",
    "ingredients_raw", "ingredients", "cooking_steps"
}

CATEGORY_KIND_NORMALIZE = {
    "밥,죽,떡": "밥/죽/떡",
    "밥/죽": "밥/죽/떡",
    "밥•죽•떡": "밥/죽/떡",
}

NOISE_TOKENS = {"구매", "기타", "그외", "그 외", ""}

# =========================
# 유틸 함수
# =========================
def progress(msg, step, total_steps):
    percent = int((step / total_steps) * 100)
    print(f"[{percent:3d}%] {msg}")
    sys.stdout.flush()

def safe_get(d, key, default=None):
    return d.get(key, default)

def keep_core_fields(doc):
    out = {}
    for k in KEEP_FIELDS:
        if k in ["view_count", "recommend_count", "scrap_count"]:
            out[k] = doc.get(k, 0) or 0   # 숫자 필드는 0
        elif k == "ingredients":
            out[k] = doc.get(k, []) or [] # 리스트는 []
        elif k == "cooking_steps":
            steps = doc.get(k, [])
            new_steps = []
            if isinstance(steps, list):
                for s in steps:
                    if not isinstance(s, dict):
                        continue
                    # step을 int로 강제 변환
                    raw_step = s.get("step", 0)
                    try:
                        step_val = int(raw_step)
                    except (ValueError, TypeError):
                        step_val = 0
                    new_steps.append({
                        "step": step_val,
                        "description": s.get("description", ""),
                        "imageUrl": s.get("imageUrl", "")
                    })
            out[k] = new_steps
        else:
            out[k] = doc.get(k, "") or "" # 기본은 빈 문자열
    return out

def normalize_category_kind(kind):
    if not isinstance(kind, str):
        return kind
    return CATEGORY_KIND_NORMALIZE.get(kind.strip(), kind.strip())

def clean_ingredients_raw(s):
    if not isinstance(s, str):
        return ""
    tmp = [t.strip() for t in s.replace(",", "|").replace("•", "|").split("|")]
    cleaned = []
    for t in tmp:
        t = t.replace("구매", "").strip()
        if not t:
            continue
        for noise in NOISE_TOKENS:
            if noise and t.strip() == noise:
                t = ""
                break
        if t:
            cleaned.append(t)
    return " | ".join(cleaned)

def clean_ingredients_list(arr):
    if not isinstance(arr, list):
        return []
    cleaned, seen = [], set()
    for x in arr:
        if not isinstance(x, str):
            continue
        t = x.strip().replace("구매", "").strip()
        if not t or t in NOISE_TOKENS:
            continue
        t = " ".join(t.split())
        if t and t not in seen:
            seen.add(t)
            cleaned.append(t)
    return cleaned

def has_required_images(doc):
    rep = safe_get(doc, "imageUrl", "")
    if not isinstance(rep, str) or not rep.strip():
        return False
    steps = safe_get(doc, "cooking_steps", [])
    if not isinstance(steps, list):
        return False
    for s in steps:
        url = safe_get(s, "imageUrl", "")
        if isinstance(url, str) and url.strip():
            return True
    return False

def compute_popularity_score(view, reco, scrap, max_view, max_reco, max_scrap):
    v = (view / max_view) if max_view else 0.0
    r = (reco / max_reco) if max_reco else 0.0
    s = (scrap / max_scrap) if max_scrap else 0.0
    return W_VIEW * v + W_RECO * r + W_SCRAP * s

def dedup_by_id(docs):
    seen, out = set(), []
    for d in docs:
        rid = safe_get(d, "RCP_SNO")
        if rid in seen:
            continue
        seen.add(rid)
        out.append(d)
    return out

# =========================
# 메인
# =========================
def main():
    random.seed(RANDOM_SEED)
    total_steps = 8
    step = 0

    # -------- 0) JSON 파일 로드
    all_files = glob.glob(str(Path(INPUT_DIR) / "*.json"))
    assert all_files, f"폴더에 JSON 파일이 없습니다: {INPUT_DIR}"

    data = []
    for fname in all_files:
        with open(fname, "r", encoding="utf-8") as f:
            try:
                chunk = json.load(f)
                if isinstance(chunk, list):
                    data.extend(chunk)
            except Exception as e:
                print(f"[경고] {fname} 로드 실패: {e}")
    step += 1
    progress("JSON 파일 로드 완료", step, total_steps)

    # -------- 1) 정제
    refined = []
    for i, doc in enumerate(data, 1):
        core = keep_core_fields(doc)
        core["category_kind"] = normalize_category_kind(core.get("category_kind"))
        core["ingredients_raw"] = clean_ingredients_raw(core.get("ingredients_raw"))
        core["ingredients"] = clean_ingredients_list(core.get("ingredients"))
        refined.append(core)
        if i % 20000 == 0:
            print(f"  -> 정제 진행중 {i:,} / {len(data):,}")
    step += 1
    progress("핵심 필드 정제 완료", step, total_steps)

    # -------- 2) 이미지 조건 필터링
    refined = [d for d in refined if has_required_images(d)]
    step += 1
    progress("이미지 조건 필터링 완료", step, total_steps)

    # -------- 3) 인기 점수 계산
    max_view = max((safe_get(d, "view_count", 0) or 0) for d in refined) if refined else 0
    max_reco = max((safe_get(d, "recommend_count", 0) or 0) for d in refined) if refined else 0
    max_scrap = max((safe_get(d, "scrap_count", 0) or 0) for d in refined) if refined else 0
    for d in refined:
        v = safe_get(d, "view_count", 0) or 0
        r = safe_get(d, "recommend_count", 0) or 0
        s = safe_get(d, "scrap_count", 0) or 0
        d["_popularity_score"] = compute_popularity_score(v, r, s, max_view, max_reco, max_scrap)
    step += 1
    progress("인기 점수 계산 완료", step, total_steps)

    # -------- 4) 인기 60%
    n_popular = min(int(TARGET_TOTAL * POPULAR_RATIO), len(refined))
    refined_sorted = sorted(refined, key=lambda x: x["_popularity_score"], reverse=True)
    popular_top = refined_sorted[:n_popular]
    popular_ids = {d["RCP_SNO"] for d in popular_top if "RCP_SNO" in d}
    step += 1
    progress("인기 레시피 추출 완료", step, total_steps)

    # -------- 5) 카테고리 균형 40%
    remaining_pool = [d for d in refined if d.get("RCP_SNO") not in popular_ids]
    n_balanced = min(TARGET_TOTAL - len(popular_top), len(remaining_pool))

    cat_counts = Counter(d.get("category_kind") for d in refined)
    total_for_ratio = sum(cat_counts.values()) or 1

    per_cat_target, acc = {}, 0
    for cat, cnt in cat_counts.items():
        t = math.floor(cnt / total_for_ratio * n_balanced)
        per_cat_target[cat] = t
        acc += t

    deficit = n_balanced - acc
    if deficit > 0:
        for cat, _ in sorted(cat_counts.items(), key=lambda kv: kv[1], reverse=True):
            if deficit <= 0: break
            per_cat_target[cat] = per_cat_target.get(cat, 0) + 1
            deficit -= 1

    random.shuffle(remaining_pool)
    cat_buckets = defaultdict(list)
    for d in remaining_pool:
        cat_buckets[d.get("category_kind")].append(d)

    balanced_pick = []
    for cat, target_n in per_cat_target.items():
        if target_n <= 0: continue
        bucket = cat_buckets.get(cat, [])
        take_n = min(target_n, len(bucket))
        balanced_pick.extend(bucket[:take_n])

    if len(balanced_pick) < n_balanced:
        need = n_balanced - len(balanced_pick)
        picked_ids = {d.get("RCP_SNO") for d in balanced_pick}
        filler = [d for d in remaining_pool if d.get("RCP_SNO") not in picked_ids]
        balanced_pick.extend(filler[:need])
    step += 1
    progress("카테고리 균형 샘플링 완료", step, total_steps)

    # -------- 6) 합치기 + 보정
    merged = dedup_by_id(popular_top + balanced_pick)
    if len(merged) > TARGET_TOTAL:
        merged = merged[:TARGET_TOTAL]
    elif len(merged) < TARGET_TOTAL:
        need = TARGET_TOTAL - len(merged)
        extra_pool = [d for d in refined_sorted if d.get("RCP_SNO") not in {x.get("RCP_SNO") for x in merged}]
        merged.extend(extra_pool[:need])
    for d in merged:
        d.pop("_popularity_score", None)
    step += 1
    progress("데이터 병합 및 보정 완료", step, total_steps)

    # -------- 7) 분할 저장 (5만 + 5만)
    half = TARGET_TOTAL // 2
    part1, part2 = merged[:half], merged[half:]

    out1 = Path(OUTPUT_DIR) / "final_recipes_part1.json"
    out2 = Path(OUTPUT_DIR) / "final_recipes_part2.json"

    with out1.open("w", encoding="utf-8") as f:
        json.dump(part1, f, ensure_ascii=False, indent=2)
    with out2.open("w", encoding="utf-8") as f:
        json.dump(part2, f, ensure_ascii=False, indent=2)

    step += 1
    progress("5만 + 5만 분할 저장 완료", step, total_steps)

    uniq_cats = Counter(d.get("category_kind") for d in merged)
    print(f"[완료] 저장 경로: {out1.resolve()} , {out2.resolve()}")
    print(f"- 총 {len(merged):,} 레코드 (각 50,000)")
    print(f"- 카테고리 분포 예시: {uniq_cats.most_common(10)}")

if __name__ == "__main__":
    main()
