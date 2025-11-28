import os
import json
import time
import firebase_admin
from firebase_admin import credentials, firestore
from tqdm import tqdm
from google.api_core.exceptions import GoogleAPIError
from multiprocessing import Pool, cpu_count

# ========================
# 설정
# ========================
SERVICE_ACCOUNT_PATH = r"./serviceAccountKey.json"

JSON_FILES = [
    r"C:\Recipe\final_recipes_part1.json",
    r"C:\Recipe\final_recipes_part2.json"
]

COLLECTION_NAME = "recipes"
BATCH_SIZE = 500
MAX_RETRIES = 3
RETRY_DELAY = 3  # 초
CHECKPOINT_FILE = r"./upload_checkpoint.json"

# ========================
# Firestore 초기화
# ========================
if not firebase_admin._apps:
    cred = credentials.Certificate(SERVICE_ACCOUNT_PATH)
    firebase_admin.initialize_app(cred)
db = firestore.client()

# ========================
# 체크포인트 로드/저장
# ========================
def load_checkpoint():
    if os.path.exists(CHECKPOINT_FILE):
        with open(CHECKPOINT_FILE, "r", encoding="utf-8") as f:
            return json.load(f)
    return {}

def save_checkpoint(checkpoint):
    with open(CHECKPOINT_FILE, "w", encoding="utf-8") as f:
        json.dump(checkpoint, f, ensure_ascii=False, indent=2)

# ========================
# Firestore 배치 커밋 (재시도 포함)
# ========================
def commit_with_retry(batch, attempt=1):
    try:
        batch.commit()
        return True
    except GoogleAPIError as e:
        if attempt <= MAX_RETRIES:
            print(f"[WARN] Firestore 배치 커밋 실패 (시도 {attempt}/{MAX_RETRIES}) → {RETRY_DELAY}s 대기 후 재시도, 에러: {e}")
            time.sleep(RETRY_DELAY)
            return commit_with_retry(batch, attempt + 1)
        else:
            print(f"[ERROR] Firestore 배치 커밋 최종 실패. 건너뜀. 에러: {e}")
            return False

# ========================
# 파일 업로드 함수
# ========================
def upload_file(file_path):
    checkpoint = load_checkpoint()
    start_index = checkpoint.get(os.path.basename(file_path), 0)

    with open(file_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    total_docs = len(data)
    print(f"[INFO] {file_path} → {total_docs} 레코드 업로드 시작 (체크포인트 {start_index}/{total_docs})")

    batch = db.batch()
    count = 0

    for i, doc in enumerate(tqdm(data[start_index:], desc=os.path.basename(file_path), unit="doc", initial=start_index, total=total_docs)):
        doc_id = str(doc.get("RCP_SNO", f"{os.path.basename(file_path)}_{i}"))
        doc_ref = db.collection(COLLECTION_NAME).document(doc_id)
        batch.set(doc_ref, doc)
        count += 1

        if (i + 1 + start_index) % BATCH_SIZE == 0:
            success = commit_with_retry(batch)
            if success:
                checkpoint[os.path.basename(file_path)] = i + 1 + start_index
                save_checkpoint(checkpoint)
                print(f"  -> {i+1+start_index}/{total_docs} ({((i+1+start_index)/total_docs)*100:.2f}%) 완료")
            batch = db.batch()

    if count % BATCH_SIZE != 0:
        success = commit_with_retry(batch)
        if success:
            checkpoint[os.path.basename(file_path)] = total_docs
            save_checkpoint(checkpoint)
            print(f"  -> {total_docs}/{total_docs} (100.00%) 완료")

    print(f"[DONE] {file_path} 업로드 완료 ({total_docs} 레코드)")

# ========================
# 메인 (병렬 실행)
# ========================
if __name__ == "__main__":
    workers = min(len(JSON_FILES), cpu_count())
    print(f"[INFO] 병렬 업로드 시작 (workers={workers})")

    with Pool(processes=workers) as pool:
        pool.map(upload_file, JSON_FILES)

    print("[ALL DONE] 모든 JSON 업로드 완료")
