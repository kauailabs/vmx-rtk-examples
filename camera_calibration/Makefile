SRCS=$(wildcard *.cpp)
OBJS=$(SRCS:.cpp=.o)
DEPS=$(SRCS:.cpp=.d)
BINS=$(SRCS:.cpp=)
SCRIPTS=$(SRCS:.cpp=.sh)

OPENCV_LIB_DIR=/usr/local/lib

CXX=g++
CXXFLAGS=-O0 -g3 -Wall  -fmessage-length=0 -std=c++11 -g -rdynamic -MMD
LDFLAGS=-L$(OPENCV_LIB_DIR) -lopencv_imgproc -lopencv_imgcodecs -lopencv_highgui -lopencv_calib3d -lopencv_features2d -lopencv_core -lopencv_videoio -lrt -lpthread

all: $(BINS)

$(BINS): $(OBJS)
	g++ $< -o $@ $(CXXFLAGS) $(LDFLAGS) 

.PHONY: clean run

clean:
	$(RM) $(OBJS) $(DEPS) $(BINS)

run:
	./$(SCRIPTS)
