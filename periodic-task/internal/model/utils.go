package model

func calculateOffset(index uint64, pages uint64) uint64 {
	return (index - 1) * pages
}
