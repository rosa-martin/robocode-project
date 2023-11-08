import sys


if __name__ == "__main__":
	itrt = 0
	for i, arg in enumerate(sys.argv):
		if i == 0:
			continue
		with open(arg, 'rb') as f:
			while True:
				data = f.read(8)
				if not data:
					break
				itrt += 1

		print(arg + " " + str(itrt))
		itrt = 0