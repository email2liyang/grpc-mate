clean:
	mkdir -p java_generated && rm -rf java_generated/*
gen: clean
	protoc --java_out=java_generated *.proto