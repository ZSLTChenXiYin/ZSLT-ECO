package service

import (
	"math/rand"
	"time"
)

const (
	letter_bytes  = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
	special_bytes = "!@#$%^&*()_+-=[]{}|;:,.<>?"
	digit_bytes   = "0123456789"
	all_bytes     = letter_bytes + special_bytes + digit_bytes
)

var seeded_rand *rand.Rand = rand.New(rand.NewSource(time.Now().UnixNano()))

func randStringBytes(bytes string, n int) string {
	b := make([]byte, n)
	bytes_length := len(bytes)
	for i := range b {
		b[i] = bytes[seeded_rand.Intn(bytes_length)]
	}
	return string(b)
}
