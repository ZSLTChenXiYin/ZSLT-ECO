# 用户服务

## 服务说明
### gRPC 接口
### 使用说明
### HTTP 接口（开发中）

## 构建服务
```bash
go build -ldflags "-s -w -X main.Name=user -X main.Version=1.0.0 -X main.Build=$(date +%Y%m%d) -X 'main.id=user-ChenXiYin-$(date +%s)'" -o user ./cmd/user
```

## 运行服务
```bash
./user -conf ./configs/config.yaml
```
