# 服务说明
## gRPC 接口
* [创建邮箱](#gRPC-创建邮箱)
* [创建邮件模板](#gRPC-创建邮件模板)
* [发送邮件](#gRPC-发送邮件)
* [附加模板发送邮件](#gRPC-附加模板发送邮件)

## 使用说明
1. 创建邮箱，该邮箱用于随机选取作为发送方邮箱，数据库中至少存在一个邮箱才能正常提供服务
2. 创建邮件模板，该模板用于发送邮件中使用，模板中包含变量，变量使用 {{.变量名}} 语法，变量名必须与模板参数一致
3. 发送邮件，Args 参数用于填充模板变量，仅支持 JSON 格式
4. 附加模板发送邮件，该接口用于发送邮件时附加临时模板，该接口与发送邮件接口一致

## HTTP 接口（开发中）
* 创建邮箱
* 获取邮箱列表（扩展）
* 更新邮箱（扩展）
* 删除邮箱（扩展）
* 创建邮件模板
* 获取邮件模板列表（扩展）
* 更新邮件模板（扩展）
* 删除邮件模板（扩展）
* 发送邮件
* 附加模板发送邮件

## 接口详情
### gRPC 接口
#### gRPC 创建邮箱
```proto
service Email {
	rpc CreateEmail (CreateEmailRequest) returns (CreateEmailReply);
}
message CreateEmailRequest {
	string email = 1;
	string password = 2;
}
message CreateEmailReply {
	bool success = 1;
}
```
#### gRPC 创建邮件模板
```proto
service Email {
	rpc CreateEmailTemplate (CreateEmailTemplateRequest) returns (CreateEmailTemplateReply);
}
message CreateEmailTemplateRequest {
	string name = 1;
	_Json args = 2;
	string content = 3;
}
message CreateEmailTemplateReply {
	uint64 template_id = 1;
}
```
#### gRPC 发送邮件
```proto
service Email {
	rpc SendEmail (SendEmailRequest) returns (SendEmailReply);
}
message SendEmailRequest {
	uint64 template_id = 1;
	string from = 2;
	string to = 3;
	string subject = 4;
	bool use_default_args = 5;
	_Json args = 6;
}
message SendEmailReply {
	bool success = 1;
}
```
#### gRPC 附加模板发送邮件
```proto
service Email {
	rpc SendEmailWithTemplate (SendEmailWithTemplateRequest) returns (SendEmailWithTemplateReply);
}
message SendEmailWithTemplateRequest {
	string content = 1;
	string from = 2;
	string to = 3;
	string subject = 4;
	_Json args = 5;
}
message SendEmailWithTemplateReply {
	bool success = 1;
}
```