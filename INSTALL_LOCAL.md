# 로컬 설치 안내 (로컬 Claude / 사람 공용)

갤럭시워치 단어장 앱을 **폰**과 **시계**에 설치한다.
APK 는 이미 빌드되어 `apk/` 폴더에 있다 (둘 다 같은 키로 서명됨 → 폰↔시계 동기화 가능).

| 파일 | 설치 대상 |
|---|---|
| `apk/phone.apk` | 갤럭시 폰 |
| `apk/watch.apk` | 갤럭시워치 |

---

## 로컬 Claude 에게 줄 프롬프트

```
github.com/sgh1101/word 저장소의 ccr-022e9178-my087m 브랜치를 받아서
INSTALL_LOCAL.md 대로 apk/phone.apk 는 내 폰에, apk/watch.apk 는 내 갤럭시워치에 adb 로 설치해줘.
adb 가 없으면 설치부터 하고, 시계 페어링 코드나 IP:포트가 필요하면 나한테 물어봐.
```

---

## 명령어

### 0. 저장소 받기
```bash
git clone -b ccr-022e9178-my087m https://github.com/sgh1101/word.git
cd word
```

### 1. adb 설치 (없을 때만)
```bash
# Windows (PowerShell)
winget install Google.PlatformTools
# macOS
brew install android-platform-tools
# Ubuntu/Debian
sudo apt install -y adb
```
확인: `adb version`

### 2. 폰 설치 (USB)
폰: 설정 → 휴대전화 정보 → 소프트웨어 정보 → **빌드번호 7번 탭** → 개발자 옵션 → **USB 디버깅 켜기**, USB 로 PC 연결 후 폰에서 "허용".
```bash
adb devices                       # 폰이 device 로 보이면 OK
adb -d install -r apk/phone.apk
```
> USB 가 번거로우면: `apk/phone.apk` 를 폰으로 옮겨서 파일을 눌러 직접 설치해도 된다.

### 3. 시계 설치 (Wi-Fi, PC 와 같은 와이파이)
시계: 설정 → 시계 정보 → 소프트웨어 정보 → **소프트웨어 버전 7번 탭** → 개발자 옵션 →
**ADB 디버깅 켜기** + **무선 디버깅 켜기** → "새 기기 페어링" 누르면 **IP:페어링포트** 와 **6자리 코드** 가 나옴.
```bash
adb pair <시계IP>:<페어링포트>          # 6자리 코드 입력
adb connect <시계IP>:<연결포트>         # 무선 디버깅 첫 화면에 보이는 IP:포트 (페어링 포트와 다름)
adb devices                             # <시계IP>:<포트>  device
adb -s <시계IP>:<연결포트> install -r apk/watch.apk
```

### 4. 확인
1. 폰에서 **워치 단어장** 앱 열고 단어 추가 (또는 "목록 붙여넣기")
2. 시계에서 **단어장** 앱 열기 → 글자 버튼이 뜨면 성공
3. 안 뜨면: 폰 앱 오른쪽 위 `⋮` → **시계로 다시 보내기** → 시계 앱 다시 열기

### 문제 해결
| 증상 | 해결 |
|---|---|
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | 예전 버전 삭제 후 재설치: `adb -s <기기> uninstall com.sgh.wordwatch` |
| `adb pair` 실패 | 시계 화면의 페어링 코드가 바뀌었는지 확인, 다시 "새 기기 페어링" |
| `adb connect` 실패 | PC 와 시계가 같은 와이파이인지, 시계 블루투스만 연결된 상태는 아닌지 확인 |
| 시계에 단어가 안 옴 | 폰·시계 둘 다 이 폴더의 APK 로 설치했는지 확인 (서명 달라지면 동기화 안 됨) |

### (선택) 직접 빌드할 때
Android SDK 필요. `./gradlew :mobile:assembleDebug :wear:assembleDebug`
→ 이 경우 폰·시계 **둘 다** 새로 빌드한 걸로 설치해야 한다.
