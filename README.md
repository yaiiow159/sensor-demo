# 感應器監控模擬器

#### 主要展示各感應器在各時間點上的數據 (圖表呈現)
#### 新增(系統監控) 動態偵測系cpu使用率還有jvm使用率、請求數等圖表分悉數據
#### 導入open-ai 讓系統可以使用Ai-小幫手功能
#### 告警通知 當任一感應器超出闊值時發出郵件通知
#### 加入Knife4j  + openapi3 配置可以讓後端直接測試數據

### 使用技術棧 
#### JDK版本 17版以上 請確認本機上JDk 版本是否正確 
> 確認本機環境變數是否配置正確jdk版本
~~~
java --version  
~~~
#### 後端 :   springboot3
#### 前端渲染: thymeleaf + html + css + ajax
#### 消息對列: kafka + zookeeper (單一broker)
#### 配置加密  Jasypt 
#### 監控+告警:    actuator + prometheus + alertmanager
#### 資料庫:  mysql8
#### 緩存:    redis6
#### 容器化:  docker-compose 
#### AI相關: Ollama + qwen2:1.5b模型

目前第二版待修正問題

- knife4j與openapi3 DTO Model顯示不出來
- 改為使用prometheus處理告警通知
- DB增加view加快查詢效能 以及增加partition分區 區分日期查詢加快效能
- redis ttl時間增加至1天 減少db查詢次數

<hr>

### 使用說明

1. **STEP1**: git下載本項目
   
~~~
git clone https://github.com/yaiiow159/sensor-demo.git
~~~

2. **STEP2**: 安裝mysql於本機或是docker容器上

   > **注意:** password記得在application.properties改成自己設置的密碼

~~~
docker run --name mysql --port 3306:3306 -e MYSQL_ROOT_PASSWORD=your-password -d mysql:8.0
~~~
   
3. **STEP3**: 在項目目錄上或是在IDE terminal上執行docker-compose指令

執行啟動各容器
~~~
docker-compose up -d
~~~

停止刪除各容器
~~~
docker-compose down
~~~

停止各容器
~~~
docker-compose stop
~~~

<hr>

4. **STEP4**: 在IDE執行項目 有區分dev跟test版本 可以自行在application.properties當中設置
   ~~~
   # 開發用
   spring.profile.active=dev
   
   #測試用
   spring.prifile.active=test 測試用
   ~~~
  
  > **注意** 執行前確認各容器運行是否正常

![確認個容器運行正常](https://github.com/user-attachments/assets/ae89d273-f1c9-43bf-aa7b-819192fbdb32)

- 輸入 <http://localhost:9090> 確認promethues 啟動正常 且配置正確
  ![普羅米修斯](https://github.com/user-attachments/assets/b302b55a-0841-46c5-9098-47553e5ffecd)

- 輸入 <http://localhost:9093> 確認alertmanager 啟動正常 且配置正確
  ![告警中心](https://github.com/user-attachments/assets/27addb19-a317-43a0-9bc0-edfd494b51a2)

- 確認kafka 運行正常 與zookeeper連線正常 (kafka 需將broker以及各項狀態數據紀錄至zookeeper保存確保附載均衡以及一致性)

項目運行時會顯示kafak consumer註冊狀況
  ![kafka 消費者註冊訊息](https://github.com/user-attachments/assets/16a83ff7-6df0-4690-a37f-a4d18a516329)

- 查看knife4j或是openapi 是否可正常開啟
  ![knife4j](https://github.com/user-attachments/assets/8e9ea9f8-bc0d-4314-89cc-8b5c96c478a0)
  ![openapi3](https://github.com/user-attachments/assets/e170d617-2c9b-487e-bc5a-ef3ced7c454d)

- 啟動項目地址 <http:localhost:8080> 確認前端頁面顯示正常
  ![前端頁面](https://github.com/user-attachments/assets/dd600cef-35e6-4bba-bb4e-4293cd9bb6d0)

5. **STEP5** 測試各項功能是否正常
   ![首頁](https://github.com/user-attachments/assets/9931acd5-5d25-41c2-8d7e-e7333e175ca7)
   ![儀錶板總攬](https://github.com/user-attachments/assets/775023be-1d86-4fee-83cf-41e1c4afb33d)
   ![美日分析](https://github.com/user-attachments/assets/bfe503e7-a023-4e71-b337-7ebb7d46a936)
   ![每小時分析](https://github.com/user-attachments/assets/178b5d04-b9e9-4edd-85d2-bc1f486b6291)
   ![離峰時段分析](https://github.com/user-attachments/assets/133c23c8-98be-41b3-a33c-71591cc07b97)
   ![尖峰時段分析](https://github.com/user-attachments/assets/0515db11-c09d-4f92-92b8-b04d2792c2d9)
   ![緩存監控](https://github.com/user-attachments/assets/dc9f1a1a-4733-4265-b71a-4f73e65bb924)

   > **注意** 該功能尚在開發中 點及提交會跳出警示顯示
   ![AI功能測試](https://github.com/user-attachments/assets/a8244efa-212f-4465-a115-23b9fd6e4771)


   > **注意** AI的API_KEY可自行在application.properties當中設置程自己的金鑰
   ![系統監控](https://github.com/user-attachments/assets/3a8289d1-fcc0-4a3e-90af-fac0884154ae)


   
   
   


