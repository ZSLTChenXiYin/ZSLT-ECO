package service

import (
	"context"

	pb "email/api/email/v1"
	"email/internal/biz"
	"email/internal/conf"
	"email/internal/model"
)

type EmailService struct {
	pb.UnimplementedEmailServer

	uc *biz.EmailUsecase
	ec *conf.Email
}

func NewEmailService(uc *biz.EmailUsecase, ec *conf.Email) *EmailService {
	return &EmailService{uc: uc, ec: ec}
}

func (s *EmailService) CreateEmail(ctx context.Context, req *pb.CreateEmailRequest) (*pb.CreateEmailReply, error) {
	reply := &pb.CreateEmailReply{
		Success: false,
	}

	err := s.uc.CreateEmail(ctx, req.Email, req.Password)
	if err != nil {
		return reply, err
	}

	reply.Success = true

	return reply, nil
}
func (s *EmailService) CreateEmailTemplate(ctx context.Context, req *pb.CreateEmailTemplateRequest) (*pb.CreateEmailTemplateReply, error) {
	reply := &pb.CreateEmailTemplateReply{
		TemplateId: 0,
	}

	template_id, err := s.uc.CreateEmailTemplate(ctx, req.Name, req.Args.Json, req.Content)
	if err != nil {
		return reply, err
	}

	reply.TemplateId = template_id

	return reply, nil
}
func (s *EmailService) SendEmail(ctx context.Context, req *pb.SendEmailRequest) (*pb.SendEmailReply, error) {
	reply := &pb.SendEmailReply{
		Success: false,
	}

	email, err := s.uc.GetRandomEmail(ctx)
	if err != nil {
		return reply, err
	}

	template, err := s.uc.GetEmailTemplate(ctx, req.TemplateId)
	if err != nil {
		return reply, err
	}

	err = sendEmail(
		s.ec.Host,
		s.ec.Port,
		req.From,
		req.To,
		req.Subject,
		req.UseDefaultArgs,
		req.Args.Json,
		email,
		template,
	)
	if err != nil {
		return reply, err
	}

	reply.Success = true

	return reply, nil
}
func (s *EmailService) SendEmailWithTemplate(ctx context.Context, req *pb.SendEmailWithTemplateRequest) (*pb.SendEmailWithTemplateReply, error) {
	reply := &pb.SendEmailWithTemplateReply{
		Success: false,
	}

	email, err := s.uc.GetRandomEmail(ctx)
	if err != nil {
		return reply, err
	}

	err = sendEmail(
		s.ec.Host,
		s.ec.Port,
		req.From,
		req.To,
		req.Subject,
		false,
		req.Args.Json,
		email,
		&model.EmailTemplate{
			Name:    "null",
			Content: req.Content,
		},
	)
	if err != nil {
		return reply, err
	}

	reply.Success = true

	return reply, nil
}
