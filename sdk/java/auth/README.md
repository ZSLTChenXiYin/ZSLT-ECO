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

## 接口详情
### gRPC 接口
```
#### gRPC 获取Token
```proto
service AuthService {
	rpc GetToken (GetTokenRequest) returns (GetTokenResponse);
}
message GetTokenRequest {
	string user_id = 1;
	string server_name = 2;
}
message GetTokenResponse {
	bool success = 1;
	string message = 2;
	string access_token = 3;
	string refresh_token = 4;
	string user_id = 5;
}
```
#### gRPC 刷新Token
```proto
service AuthService {
	rpc RefreshToken (RefreshTokenRequest) returns (RefreshTokenResponse);
}
message RefreshTokenRequest {
	string refresh_token = 1;
}
message RefreshTokenResponse {
	bool success = 1;
	string message = 2;
	string access_token = 3;
	string refresh_token = 4;
}

```
#### gRPC 撤销Token
```proto
service AuthService {
	rpc RevokeToken (RevokeTokenRequest) returns (RevokeTokenResponse);
}
message RevokeTokenRequest {
	string user_id = 1;
	string server_name = 2;
	string token_category = 3;
}
message RevokeTokenResponse {
	bool success = 1;
	string message = 2;
}

```
### gRPC 验证Token
```proto
service AuthService {
	rpc ValidateToken (ValidateTokenRequest) returns (ValidateTokenResponse);
}
message ValidateTokenRequest {
	string token = 1;
	string token_category = 2;
}
message ValidateTokenResponse {
	bool success = 1;
	string message = 2;
	bool valid = 3;
	string user_id = 4;
	string server_name = 5;
}
```

