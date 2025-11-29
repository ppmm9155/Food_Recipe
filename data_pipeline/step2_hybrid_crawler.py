import json
import requests
from bs4 import BeautifulSoup
import time
from tqdm import tqdm
import re
import random
import os

# --- 설정값 ---
# (입력 파일) 1단계에서 만든 '쇼핑 리스트' JSON
INPUT_JSON_FILE = 'all_recipes_for_firestore.json'
# (중간 저장) 1000개마다 중간 결과를 저장합니다.
CHECKPOINT_INTERVAL = 1000
# (중간 저장 폴더) 중간 결과 파일들이 저장될 폴더 이름
CHECKPOINT_DIR = 'crawling_checkpoints'
# ----------------

# '진짜 브라우저'인 척하는 헤더 정보
headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36',
    'Referer': 'https://www.google.com/'
}

# 중간 저장 폴더가 없으면 새로 생성합니다.
if not os.path.exists(CHECKPOINT_DIR):
    os.makedirs(CHECKPOINT_DIR)
    print(f"'{CHECKPOINT_DIR}' 폴더를 생성했습니다.")

# --- '이어하기' 기능: 이미 처리된 레시피 목록을 확인합니다. ---
processed_ids = set()
checkpoint_files = os.listdir(CHECKPOINT_DIR)
if checkpoint_files:
    print("기존에 저장된 체크포인트 파일을 확인합니다...")
    for file_name in tqdm(checkpoint_files, desc="진행 상황 로딩 중"):
        file_path = os.path.join(CHECKPOINT_DIR, file_name)
        with open(file_path, 'r', encoding='utf-8') as f:
            partial_data = json.load(f)
            for item in partial_data:
                processed_ids.add(item['RCP_SNO'])
    print(f"총 {len(processed_ids)}개의 레시피가 이미 처리되었습니다. 이어서 크롤링을 시작합니다.")

# --- '쇼핑 리스트'를 불러옵니다. ---
try:
    with open(INPUT_JSON_FILE, 'r', encoding='utf-8') as f:
        all_recipes = json.load(f)
except FileNotFoundError:
    print(f"❌ 오류: '{INPUT_JSON_FILE}'을 찾을 수 없습니다.")
    raise SystemExit("스크립트 중단")

# 아직 처리되지 않은 레시피 목록만 골라냅니다.
recipes_to_crawl = [r for r in all_recipes if r['RCP_SNO'] not in processed_ids]

if not recipes_to_crawl:
    print("\n 모든 레시피의 크롤링이 이미 완료되었습니다! 'merge_files.py'를 실행하여 최종 파일을 만드세요.")
    raise SystemExit("작업 종료")

print(f"\n총 {len(all_recipes)}개 중, {len(recipes_to_crawl)}개의 레시피에 대한 크롤링을 시작합니다.")
print(f"주의: 작업은 언제든지 중단하고 다시 실행할 수 있습니다.")

# --- 메인 크롤링 루프 ---
partial_results = []
start_index = len(processed_ids)

for recipe in tqdm(recipes_to_crawl, desc="레시피 크롤링/복원 중"):
    rcp_sno = recipe.get('RCP_SNO')
    if not rcp_sno: continue

    url = f"https://www.10000recipe.com/recipe/{rcp_sno}"

    try:
        response = requests.get(url, headers=headers)
        if response.status_code == 200:
            soup = BeautifulSoup(response.text, 'html.parser')

            # ==========================================================
            # [최종 하이브리드] '보여주기용'과 '검색용'을 각자 최고의 방식으로 처리합니다.
            # ==========================================================
            ingre_div = soup.find('div', class_='ready_ingre3')
            if ingre_div:
                scraped_ingredients_with_amount = [] # '재료 이름 + 양' 목록 (보여주기용)
                scraped_ingredient_names = []      # '재료 이름'만 있는 목록 (검색용)
                
                for li in ingre_div.find_all('li'):
                    # 1. <li> 태그 안의 텍스트를 가져와 '구매' 단어를 제거하고 정리합니다.
                    raw_text = li.get_text()
                    cleaned_text = ' '.join(raw_text.replace('구매', '').split())
                    if not cleaned_text: continue

                    # 2. [보여주기용] 정리된 전체 텍스트를 그대로 저장합니다.
                    scraped_ingredients_with_amount.append(cleaned_text)

                    # 3. [검색용] 전체 텍스트에서 '양/단위' 부분을 제거하여 '이름'만 추출합니다.
                    amount_tag = li.find('span', class_='ingre_unit')
                    amount = amount_tag.get_text(strip=True) if amount_tag else ""
                    name = cleaned_text.replace(amount, '').strip()
                    
                    # <a> 태그가 있는 경우, 더 깨끗한 이름을 얻기 위해 한 번 더 정제합니다.
                    name_link = li.find('a')
                    if name_link:
                        name = name_link.get_text(strip=True)
                    
                    if name:
                        scraped_ingredient_names.append(name)
                
                if scraped_ingredients_with_amount:
                    recipe['ingredients_raw'] = f"[재료] {' | '.join(scraped_ingredients_with_amount)}"
                    recipe['ingredients'] = sorted(list(set(scraped_ingredient_names)))
            # ==========================================================

            # --- 기존 임무: imageUrl 및 cooking_steps 채우기 ---
            if not recipe.get('imageUrl'):
                main_img = soup.find('img', id='main_thumbs')
                if main_img and 'src' in main_img.attrs: recipe['imageUrl'] = main_img['src']
                else:
                    main_thumb_div = soup.find('div', class_='view_pic')
                    if main_thumb_div and main_thumb_div.find('img'): recipe['imageUrl'] = main_thumb_div.find('img')['src']

            recipe['cooking_steps'] = [] # cooking_steps 초기화
            step_divs_new = soup.find_all('div', class_='view_step_cont')
            if step_divs_new:
                for index, step_div in enumerate(step_divs_new):
                    desc = step_div.find('div', class_='media-body')
                    img = step_div.find('img')
                    if desc: recipe['cooking_steps'].append({"step": index + 1, "description": desc.get_text(strip=True), "imageUrl": img['src'] if img and 'src' in img.attrs else ""})
            else:
                recipe_step_div_old = soup.find('div', id='recipe_step')
                if recipe_step_div_old:
                    elements = recipe_step_div_old.find_all(['div', 'p'])
                    text_buffer = ""
                    for el in elements:
                        text = el.get_text(strip=True)
                        if text:
                            if re.match(r'^\d+\.', text):
                                if text_buffer: recipe['cooking_steps'].append({"step": len(recipe['cooking_steps']) + 1, "description": text_buffer, "imageUrl": ""})
                                text_buffer = text
                            else: text_buffer += " " + text
                    if text_buffer: recipe['cooking_steps'].append({"step": len(recipe['cooking_steps']) + 1, "description": text_buffer, "imageUrl": ""})
                                
    except Exception as e:
        pass # 오류가 발생해도 멈추지 않고 다음으로 넘어갑니다.
    
    partial_results.append(recipe)

    # --- '자동 중간 저장' 기능 ---
    if len(partial_results) >= CHECKPOINT_INTERVAL:
        file_num = (start_index // CHECKPOINT_INTERVAL) + 1
        file_name = f"partial_results_{file_num}.json"
        with open(os.path.join(CHECKPOINT_DIR, file_name), 'w', encoding='utf-8') as f:
            json.dump(partial_results, f, ensure_ascii=False, indent=4)
        print(f"\n✅ 체크포인트 저장: {file_name}")
        partial_results = []
        start_index += CHECKPOINT_INTERVAL

    time.sleep(random.uniform(0.3, 0.8))

# --- 마지막에 남은 데이터 저장 ---
if partial_results:
    file_num = (start_index // CHECKPOINT_INTERVAL) + 1
    file_name = f"partial_results_{file_num}.json"
    with open(os.path.join(CHECKPOINT_DIR, file_name), 'w', encoding='utf-8') as f:
        json.dump(partial_results, f, ensure_ascii=False, indent=4)
    print(f"\n✅ 마지막 체크포인트 저장: {file_name}")

print("\n모든 레시피 크롤링/복원 완료! 'merge_files.py'를 실행하세요.")

