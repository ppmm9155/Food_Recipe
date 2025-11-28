import pandas as pd
import json

csv_files = [
    'TB_RECIPE_SEARCH-220701.csv',
    'TB_RECIPE_SEARCH-20231130.csv',
    'TB_RECIPE_SEARCH_241226.csv'
]

df = pd.concat([pd.read_csv(f, encoding='utf-8-sig') for f in csv_files])
df = df.drop_duplicates(subset=['RCP_SNO'])

# NaN → 빈 문자열 변환
df = df.fillna("")

records = []
for _, row in df.iterrows():
    records.append({
        'RCP_SNO': row.get('RCP_SNO'),
        'title': row.get('RCP_TTL'),
        'summary': row.get('CKG_IPDC'),
        'imageUrl': row.get('RCP_IMG_URL') or "",   # 빈 문자열 보장
        'category_kind': row.get('CKG_KND_ACTO_NM'),
        'category_material': row.get('CKG_MTRL_ACTO_NM'),
        'category_method': row.get('CKG_MTH_ACTO_NM'),
        'category_situation': row.get('CKG_STA_ACTO_NM'),
        'servings': row.get('CKG_INBUN_NM'),
        'difficulty': row.get('CKG_DODF_NM'),
        'cooking_time': row.get('CKG_TIME_NM'),
        'view_count': int(row.get('INQ_CNT') or 0),
        'recommend_count': int(row.get('RCMM_CNT') or 0),
        'scrap_count': int(row.get('SRAP_CNT') or 0),
        'ingredients_raw': "",
        'ingredients': [],
    })

with open('all_recipes_for_firestore.json', 'w', encoding='utf-8') as f:
    json.dump(records, f, ensure_ascii=False, indent=4)

print(f"✅ 변환 완료! 총 {len(records)}개 레시피 저장됨")
