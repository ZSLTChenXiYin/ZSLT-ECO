package model

import (
	"time"

	"gorm.io/datatypes"
	"gorm.io/gorm"
)

type Message struct {
	gorm.Model
	Sender      string         `gorm:"not null"`
	MessageType string         `gorm:"type:ENUM('private', 'public');not null"`
	Content     datatypes.JSON `gorm:"not null"`
}

type MessageConsumer struct {
	gorm.Model
	MessageID       uint64    `gorm:"not null"`
	ConsumerService string    `gorm:"not null"`
	Status          string    `gorm:"type:ENUM('success', 'failure', 'progress');not null;default:'progress'"`
	ConsumedAt      time.Time `gorm:"not null"`
}

type MessageOperator struct {
	db *gorm.DB
}

func NewMessageOperator(db *gorm.DB) *MessageOperator { return &MessageOperator{db: db} }

func (m *MessageOperator) GetNotConsumedMessageList(name string, pages uint64, service_name string) ([]Message, error) {
	var messages []Message

	tx := m.db.Begin()
	defer func() {
		if r := recover(); r != nil {
			tx.Rollback()
		}
	}()

	err := tx.Table(m.messageTableName(name)).
		Where("NOT EXISTS (?) OR EXISTS (?)", // 查询条件：不存在消费记录或存在失败记录
			m.db.Table(m.messageConsumerTableName(name)).
				Where(m.messageConsumerTableName(name)+".message_id = "+m.messageTableName(name)+".id").
				Where(m.messageConsumerTableName(name)+".consumer_service = ?", service_name),
			m.db.Table(m.messageConsumerTableName(name)).
				Where(m.messageConsumerTableName(name)+".message_id = "+m.messageTableName(name)+".id").
				Where(m.messageConsumerTableName(name)+".consumer_service = ?", service_name).
				Where(m.messageConsumerTableName(name)+".status = ?", "failure"),
		).
		Limit(int(pages)). // 分页限制
		Find(&messages).Error
	if err != nil {
		tx.Rollback()
		return nil, err
	}

	for _, message := range messages {
		consumer := &MessageConsumer{
			MessageID:       uint64(message.ID),
			ConsumerService: service_name,
			Status:          "progress",
			ConsumedAt:      time.Now(),
		}
		err = tx.Table(m.messageConsumerTableName(name)).
			Create(consumer).Error
		if err != nil {
			tx.Rollback()
			return nil, err
		}
	}
	if err := tx.Commit().Error; err != nil {
		tx.Rollback()
		return nil, err
	}

	return messages, err
}

func (m *MessageOperator) messageTableName(name string) string { return name + "_messages" }

func (m *MessageOperator) CreateMessageTable(name string) error {
	return m.db.Table(m.messageTableName(name)).AutoMigrate(&Message{})
}

func (m *MessageOperator) CreateMessage(name string, message *Message) error {
	return m.db.Table(m.messageTableName(name)).Create(message).Error
}

func (m *MessageOperator) GetMessageList(name string, index uint64, pages uint64) ([]Message, error) {
	var messages []Message
	return messages, m.db.Table(m.messageTableName(name)).
		Offset(int(calculateOffset(index, pages))).Limit(int(pages)).
		Find(&messages).Error
}

func (m *MessageOperator) GetMessageListBySender(name string, index uint64, pages uint64, sender string) ([]Message, error) {
	var messages []Message
	return messages, m.db.Table(m.messageTableName(name)).
		Where("sender = ?", sender).
		Offset(int(calculateOffset(index, pages))).Limit(int(pages)).
		Find(&messages).Error
}

func (m *MessageOperator) DeleteMessageByID(name string, id uint64) error {
	return m.db.Table(m.messageTableName(name)).Delete(&Message{}, id).Error
}

func (m *MessageOperator) messageConsumerTableName(name string) string {
	return name + "_message_consumers"
}

func (m *MessageOperator) CreateMessageConsumerTable(name string) error {
	return m.db.Table(m.messageConsumerTableName(name)).AutoMigrate(&MessageConsumer{})
}

func (m *MessageOperator) CreateMessageConsumer(name string, consumer *MessageConsumer) error {
	return m.db.Table(m.messageConsumerTableName(name)).Create(consumer).Error
}

func (m *MessageOperator) GetMessageConsumerList(name string, index uint64, pages uint64) ([]MessageConsumer, error) {
	var consumers []MessageConsumer
	return consumers, m.db.Table(m.messageConsumerTableName(name)).
		Offset(int(calculateOffset(index, pages))).Limit(int(pages)).
		Find(&consumers).Error
}

func (m *MessageOperator) GetMessageConsumerListByMessageID(name string, index uint64, pages uint64, message_id uint64) ([]MessageConsumer, error) {
	var consumers []MessageConsumer
	return consumers, m.db.Table(m.messageConsumerTableName(name)).
		Where("message_id = ?", message_id).
		Offset(int(calculateOffset(index, pages))).Limit(int(pages)).
		Find(&consumers).Error
}

func (m *MessageOperator) GetMessageConsumerListByConsumerService(name string, index uint64, pages uint64, consumer_service string) ([]MessageConsumer, error) {
	var consumers []MessageConsumer
	return consumers, m.db.Table(m.messageConsumerTableName(name)).
		Where("consumer_service = ?", consumer_service).
		Offset(int(calculateOffset(index, pages))).Limit(int(pages)).
		Find(&consumers).Error
}

func (m *MessageOperator) UpdateMessageConsumerStatusByID(name string, id uint, status string) error {
	return m.db.Table(m.messageConsumerTableName(name)).
		Where("id = ?", id).
		Update("status", status).Error
}

func (m *MessageOperator) DeleteMessageConsumerByID(name string, id uint64) error {
	return m.db.Table(m.messageConsumerTableName(name)).Delete(&MessageConsumer{}, id).Error
}
