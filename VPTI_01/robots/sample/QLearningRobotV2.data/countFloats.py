import struct

itrt = 0

with open('weights.bin', 'rb') as f:
	while True:
		data = f.read(8)
		if not data:
			break
		#value = struck.unpack('d', data)[0]
		itrt += 1

print(itrt)