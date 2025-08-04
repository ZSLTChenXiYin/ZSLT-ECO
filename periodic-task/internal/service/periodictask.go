package service

import (
	"context"
	"encoding/json"
	"errors"
	"sync"

	pb "periodic-task/api/periodictask/v1"
	"periodic-task/internal/biz"
	"periodic-task/internal/model"

	"gorm.io/datatypes"
)

const (
	service_key_length = 32
)

var (
	periodic_task_queue_started_map = sync.Map{}
)

type PeriodicTaskService struct {
	pb.UnimplementedPeriodicTaskServer

	uc *biz.DataUsecase
}

func NewPeriodicTaskService(uc *biz.DataUsecase) *PeriodicTaskService {
	return &PeriodicTaskService{uc: uc}
}

func (s *PeriodicTaskService) ServiceRegistry(ctx context.Context, req *pb.ServiceRegistryRequest) (*pb.ServiceRegistryReply, error) {
	reply := &pb.ServiceRegistryReply{
		Secret: "",
	}

	service_operator := s.uc.NewServiceOperator()
	random_key := ""
	for {
		random_key = randStringBytes(letter_bytes+digit_bytes, service_key_length)

		exists, err := service_operator.ExistsService(req.ServiceName, random_key)
		if exists {
			continue
		} else {
			if err != nil {
				return reply, err
			} else {
				break
			}
		}
	}

	err := service_operator.CreateService(ctx, &model.Service{
		Name:   req.ServiceName,
		Secret: random_key,
		Scheme: "rpc",
	})
	if err != nil {
		return reply, err
	}

	reply.Secret = random_key

	return reply, nil
}
func (s *PeriodicTaskService) CreatePeriodicTaskQueue(ctx context.Context, req *pb.CreatePeriodicTaskQueueRequest) (*pb.CreatePeriodicTaskQueueReply, error) {
	reply := &pb.CreatePeriodicTaskQueueReply{
		Success: false,
	}

	exists, err := s.uc.NewServiceOperator().ExistsService(req.ServiceName, req.Secret)
	if !exists {
		if err != nil {
			return reply, err
		} else {
			return reply, errors.New("指定服务未注册")
		}
	}

	message_operator := s.uc.NewMessageOperator()

	err = message_operator.CreateMessageTable(req.QueueName)
	if err != nil {
		return reply, err
	}

	err = message_operator.CreateMessageConsumerTable(req.QueueName)
	if err != nil {
		return reply, err
	}

	reply.Success = true

	return reply, nil
}
func (s *PeriodicTaskService) StartPeriodicTaskQueue(ctx context.Context, req *pb.StartPeriodicTaskQueueRequest) (*pb.StartPeriodicTaskQueueReply, error) {
	reply := &pb.StartPeriodicTaskQueueReply{
		Success: false,
	}

	exists, err := s.uc.NewServiceOperator().ExistsService(req.ServiceName, req.Secret)
	if !exists {
		if err != nil {
			return reply, err
		} else {
			return reply, errors.New("指定服务未注册")
		}
	}

	_, ok := periodic_task_queue_started_map.Load(req.QueueName)
	if ok {
		return reply, errors.New("指定周期任务队列已启动")
	}

	periodic_task_queue_started_map.Store(req.QueueName, struct{}{})

	reply.Success = true

	return reply, nil
}
func (s *PeriodicTaskService) CreateTask(ctx context.Context, req *pb.CreateTaskRequest) (*pb.CreateTaskReply, error) {
	reply := &pb.CreateTaskReply{
		Success: false,
	}

	exists, err := s.uc.NewServiceOperator().ExistsService(req.ServiceName, req.Secret)
	if !exists {
		if err != nil {
			return reply, err
		} else {
			return reply, errors.New("指定服务未注册")
		}
	}

	_, ok := periodic_task_queue_started_map.Load(req.QueueName)
	if !ok {
		return reply, errors.New("指定周期任务队列未启动")
	}

	message := &model.Message{
		Sender:      req.ServiceName,
		MessageType: req.TaskType,
		Content:     datatypes.JSON(req.TaskData.Json),
	}

	err = s.uc.NewMessageOperator().CreateMessage(req.QueueName, message)
	if err != nil {
		return reply, err
	}

	reply.Success = true

	return reply, nil
}
func (s *PeriodicTaskService) GetTaskList(ctx context.Context, req *pb.GetTaskListRequest) (*pb.GetTaskListReply, error) {
	reply := &pb.GetTaskListReply{
		TaskList: &pb.XJson{
			Json: nil,
		},
	}

	exists, err := s.uc.NewServiceOperator().ExistsService(req.ServiceName, req.Secret)
	if !exists {
		if err != nil {
			return reply, err
		} else {
			return reply, errors.New("指定服务未注册")
		}
	}

	_, ok := periodic_task_queue_started_map.Load(req.QueueName)
	if !ok {
		return reply, errors.New("指定周期任务队列未启动")
	}

	message_operator := s.uc.NewMessageOperator()

	if req.Limit < 1 {
		req.Limit = 10
	}

	messages, err := message_operator.GetNotConsumedMessageList(req.QueueName, uint64(req.Limit), req.ServiceName)
	if err != nil {
		return reply, err
	}

	filtered_messages := make([]model.Message, len(messages))
	index := 0

	for _, message := range messages {
		if message.Sender == req.ServiceName {
			filtered_messages[index] = message
			index++
			continue
		}

		if message.MessageType == "public" {
			filtered_messages[index] = message
			index++
		}
	}

	messages_json, err := json.Marshal(filtered_messages[:index])
	if err != nil {
		return reply, err
	}

	reply.TaskList.Json = messages_json

	return reply, nil
}
func (s *PeriodicTaskService) CompleteTask(ctx context.Context, req *pb.CompleteTaskRequest) (*pb.CompleteTaskReply, error) {
	reply := &pb.CompleteTaskReply{
		Success: false,
	}

	exists, err := s.uc.NewServiceOperator().ExistsService(req.ServiceName, req.Secret)
	if !exists {
		if err != nil {
			return reply, err
		} else {
			return reply, errors.New("指定服务未注册")
		}
	}

	_, ok := periodic_task_queue_started_map.Load(req.QueueName)
	if !ok {
		return reply, errors.New("指定周期任务队列未启动")
	}

	status := "success"
	if !req.Success {
		status = "failure"
	}

	err = s.uc.NewMessageOperator().UpdateMessageConsumerStatusByID(req.QueueName, uint(req.TaskId), status)
	if err != nil {
		return reply, err
	}

	reply.Success = true

	return reply, nil
}
