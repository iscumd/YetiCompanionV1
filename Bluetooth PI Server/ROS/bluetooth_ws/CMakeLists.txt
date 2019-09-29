cmake_minimum_required(VERSION 2.8.3)
project(bluetooth)

find_package(catkin REQUIRED COMPONENTS
  roscpp
  std_msgs
  geometry_msgs
  message_generation
)

## Generate messages in the 'msg' folder
add_message_files(
    FILES
    bluetoothmsg.msg
)


## Generate added messages and services with any dependencies listed here
generate_messages(
   DEPENDENCIES
   geometry_msgs
   std_msgs
)

catkin_package(
  CATKIN_DEPENDS roscpp std_msgs geometry_msgs message_runtime
  )

include_directories(
    include
    ${catkin_INCLUDE_DIRS}
)

add_library(bluetoothclass src/BTServer.cpp)
target_link_libraries(bluetoothclass bluetooth)

add_executable(bluetooth_node src/bluetooth.cpp)
add_dependencies(${PROJECT_NAME}_node ${PROJECT_NAME}_generate_messages_cpp)
target_link_libraries(${PROJECT_NAME}_node bluetoothclass ${catkin_LIBRARIES})

#add_dependencies(bluetooth_node bluetoothclass)
#${PROJECT_NAME}_generate_messages_cpp)
