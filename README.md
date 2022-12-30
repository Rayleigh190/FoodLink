# 2022 - FoodLink Project

2022년 2학기 모바일응용소프트웨어 과목의 기말 프로젝트입니다.

## Description

**Project Name :**  FoodLink(푸드링크)

혼자 자취하면서 끼니를 잘 챙겨 먹는 것은 쉬운 일이 아니다. 항상 무엇을 먹어 야 하는지, 주변에 어떤 식당들이 있는지에 대해 고민한다. 이런 고민을 해결하고자 음식과 연결해 주는 서비스인 푸드링크를 개발하였다.

## Function

[**앱 시연 영상**](https://youtu.be/RwNle37LNok)

- “해 먹기” 기능을 통해 요리 레시피를 검색하여 레시피 세부 정보 및 메뉴얼을 확인할 수 있다.  
- “사 먹기” 기능을 통해 식당을 검색하고 세부 정보를 확인할 수 있다.
	- GPS로 현재 위치를 확인할 수 있다.  
	- “정확도순”, “거리순”으로 정렬할 수 있다.  
	- 웹뷰를 통해 지도 및 길찾기를 할 수 있다.

**기능 예시**
<img width="1208" alt="스크린샷 2022-12-30 오후 5 39 06" src="https://user-images.githubusercontent.com/86937253/210051266-f3a5a958-ee82-48ec-8d87-3ee5734f2584.png">
<img width="1081" alt="스크린샷 2022-12-30 오후 5 39 26" src="https://user-images.githubusercontent.com/86937253/210051263-56cce2a2-285e-429b-9e4a-277f4fbbbf64.png">


### 사용된 라이브러리 설명
(1) Volley : Android 앱의 HTTP 통신을 위한 라이브러리  
레시피, 식당 검색에 필요한 API 통신을 위해 사용했다. 관련 링크 : [바로가기](https://developer.android.com/training/volley?hl=ko)

(2) Glide : 구글에서 제작한 이미지 로딩 라이브러리  
API를 통해 받아온 이미지 링크를 ImageView에 세팅하기 위해 사용했다. 관련 링크 : [바로가기](https://github.com/bumptech/glide)

### 사용된 API 설명

(1) 공공데이터 - 조리식품의 레시피 DB  
메뉴명과 함께 요청을 보내면 관련된 레시피 정보를 응답으로 보내준다. “해 먹기” 기능의 레 시피 검색 기능을 구현하기 위해 사용했다.  
관련 링크 : [바로가기](http://www.foodsafetykorea.go.kr/api/openApiInfo.do?menu_grp=MENU_GRP31&menu_no=661&show_cnt=10&start_idx=1&svc_no=COOKRCP01)

(2) Kakao Developers API - 키워드로 장소 검색하기  
장소 검색어와 함께 요청을 보내면 관련 장소의 상세 정보들을 응답으로 보내준다. “사 먹기” 기능의 식당 검색 기능을 구현하기 위해 사용했다.  
관련 링크 : [바로가기](https://developers.kakao.com/docs/latest/ko/local/dev-guide#search-by-keyword)

(3) Kakao Developers API – 좌표로 주소 변환하기  
GPS 위도, 경도 좌표와 함께 요청을 보내면 해당하는 위치의 주소(도로명, 지번) 정보를 응답 으로 보내준다. “사 먹기” 기능에서 사용자의 현재 위치 주소를 알려주기 위해 사용했다.  
관련 링크 : [바로가기](https://developers.kakao.com/docs/latest/ko/local/dev-guide#coord-to-address)

## Environment

**Mobile Platform :** Android(minSdk 21, targetSdk 32)

## Prerequisite

    implementation 'com.android.volley:volley:1.2.1'  
    implementation 'com.github.bumptech.glide:glide:4.14.2'  
    annotationProcessor 'com.github.bumptech.glide:compiler:4.14.2'

## License
