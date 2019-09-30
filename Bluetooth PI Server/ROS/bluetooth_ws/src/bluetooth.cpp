#include <ros/ros.h>
#include <geometry_msgs/Twist.h>
#include <stdio.h>
#include <iostream>
#include "BTServer.h"
#include "bluetooth/bluetoothmsg.h"

BTServer btserver; //used for sending messages to and from Phone app
ros::Publisher btPub,btControlPub;


void TurtleCallback(const geometry_msgs::Twist& msg){
	btserver.Write(msg.linear.x,(char)46);//second is callback identifier
	printf("X vel is %f \n",msg.linear.x);
}

void GPIOCallBack(const bluetooth::bluetoothmsg::ConstPtr& msg){
	btserver.Write(msg->ESTOP,(char)1);
	btserver.Write(msg->driveModeLed,(char)2);
	btserver.Write(msg->robotechError,(char)3);
	btserver.Write(msg->GPIOInitalized,(char)4);
	btserver.Write(msg->motorVoltage,(char)5);
	btserver.Write(msg->motorCurrent,(char)6);
	btserver.Write(msg->temperature,(char)7);
	btserver.Write(msg->batteryVoltage,(char)8);
}

int main(int argc, char **argv){
	ros::init(argc, argv, "bluetooth_node");
	ros::NodeHandle nh;
	geometry_msgs::Twist msg;
	ros::Rate r(10);//update 10hz
	
	//ros::Subscriber sub = nh.subscribe("turtle1/cmd_vel",1,&TurtleCallback);
	btPub = nh.advertise<bluetooth::bluetoothmsg>("bluetooth/status", 5);
	btControlPub = nh.advertise<geometry_msgs::Twist>("turtle1/cmd_vel", 5);
	//For all the topics i want to listen too
	/*ros::Subscriber sub = nh.subscribe("turtle1/cmd_vel",1,&TurtleCallback);
	ros::Subscriber sub = nh.subscribe("turtle1/cmd_vel",1,&TurtleCallback);
	ros::Subscriber sub = nh.subscribe("turtle1/cmd_vel",1,&TurtleCallback);
	ros::Subscriber sub = nh.subscribe("turtle1/cmd_vel",1,&TurtleCallback);*/

	while(ros::ok()){
		printf("Waiting for client to connect\n");
		btserver.startServer();
		while(btserver.isConnected() && ros::ok()){//loop until connection is killed
			ros::spinOnce();
			r.sleep();
			msg.linear.x = btserver.lx;
			msg.linear.y = btserver.ly;
			msg.angular.z = btserver.az;
			btControlPub.publish(msg);
		}
		btserver.Close();
	}
	//ros::spin();
}
