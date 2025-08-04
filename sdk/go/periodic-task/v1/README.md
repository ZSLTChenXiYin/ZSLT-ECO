# 服务说明
## gRPC 接口
* [服务注册](#gRPC-服务注册)
* [创建任务队列](#gRPC-创建任务队列)
* [启动任务队列](#gRPC-启动任务队列)
* [创建任务](#gRPC-创建任务)
* [获取任务列表](#gRPC-获取任务列表)
* [完成任务](#gRPC-完成任务)

## 使用说明
1. 注册服务，注册的服务会返回Serect，Serect用于服务中需要验证服务是否合法的接口
2. 创建任务队列
3. 启动任务队列，一个周期任务服务节点只提供在当前节点上启动的任务队列，节点之间互不共享启动的任务队列信息
4. 创建任务，任务中包含的数据建议使用json格式提交
5. 获取任务列表，该接口会自动获取指定服务未获取过的或处理失败的任务，当输入的Limit为0时，默认返回10条数据
6. 完成任务，任务处理成功后，请调用该接口完成任务，完成任务后，任务会根据输入的Status进行状态更新，success表示任务处理成功，failure表示任务处理失败

## HTTP 接口（开发中）
* 服务注册
* 获取服务列表（扩展）
* 创建任务队列
* 获取任务队列列表（扩展）
* 启动任务队列
* 创建任务
* 获取任务列表
* 获取完整任务列表（扩展）
* 完成任务
* 销毁任务（扩展）
* 停止任务队列（扩展）
* 销毁任务队列（扩展）
* 服务注销（扩展）

## 接口详情
### gRPC 接口
#### gRPC 服务注册
```proto
service PeriodicTask {
	rpc ServiceRegistry (ServiceRegistryRequest) returns (ServiceRegistryReply);
}
message ServiceRegistryRequest {
	string service_name = 1;
}
message ServiceRegistryReply {
	string secret = 1;
}
```
#### gRPC 创建任务队列
```proto
service PeriodicTask {
	rpc CreatePeriodicTaskQueue (CreatePeriodicTaskQueueRequest) returns (CreatePeriodicTaskQueueReply);
}
message CreatePeriodicTaskQueueRequest {
	string service_name = 1;
	string secret = 2;
	string queue_name = 3;
}
message CreatePeriodicTaskQueueReply {
	bool success = 1;
}
```
#### gRPC 启动任务队列
```proto
service PeriodicTask {
	rpc StartPeriodicTaskQueue (StartPeriodicTaskQueueRequest) returns (StartPeriodicTaskQueueReply);
}
message StartPeriodicTaskQueueRequest {
	string service_name = 1;
	string secret = 2;
	string queue_name = 3;
}
message StartPeriodicTaskQueueReply {
	bool success = 1;
}
```
#### gRPC 创建任务
```proto
service PeriodicTask {
	rpc CreateTask (CreateTaskRequest) returns (CreateTaskReply);
}
message CreateTaskRequest {
	string service_name = 1;
	string secret = 2;
	string queue_name = 3;
	string task_type = 4;
	_Json task_data = 5;
}
message CreateTaskReply {
	bool success = 1;
}
```
#### gRPC 获取任务列表
```proto
service PeriodicTask {
	rpc GetTaskList (GetTaskListRequest) returns (GetTaskListReply);
}
message GetTaskListRequest {
	string service_name = 1;
	string secret = 2;
	string queue_name = 3;
	uint32 limit = 4;
}
message GetTaskListReply {
	_Json task_list = 1;
}
```
### gRPC 完成任务
```proto
service PeriodicTask {
	rpc CompleteTask (CompleteTaskRequest) returns (CompleteTaskReply);
}
message CompleteTaskRequest {
	string service_name = 1;
	string secret = 2;
	string queue_name = 3;
	uint64 task_id = 4;
	bool success = 5;
}
message CompleteTaskReply {
	bool success = 1;
}
```
