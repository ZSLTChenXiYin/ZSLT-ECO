# 鉴权服务

## 服务说明

### gRPC 接口（暂定）
* 获取token
* 刷新token
* 撤销token
* 验证token

### 使用说明
1. 获取token：请求参数携带用户id和请求服务名，返回access_token和refresh_token。
2. 刷新token：当access_token过期后，前端需要携带refresh_token来请求刷新access_token,当refresh_token快过期时，也会刷新refresh_token。将两个token一起返回。
3. 撤销token：请求参数携带用户id、请求服务名及token类别，会将对应token加入黑名单。
4. 验证token：会验证请求头中的‘Authorization’里携带的token。

## 构建服务
```bash
# 跳过测试打包
mvn clean package -DskipTests
```

## 运行服务
```bash
nohup java -jar target/auth-1.0.0-SNAPSHOT.jar > auth.log 2>&1 &
```
