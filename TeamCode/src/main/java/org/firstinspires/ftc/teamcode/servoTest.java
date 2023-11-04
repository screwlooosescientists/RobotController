package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

import java.util.concurrent.TimeUnit;

@TeleOp(group = "ZTest")
public class servoTest extends LinearOpMode{
    public void runOpMode() throws InterruptedException {
        Servo servo = hardwareMap.servo.get("servo1");
        servo.setDirection(Servo.Direction.REVERSE);
        waitForStart();
        if (isStopRequested()) return;
        while (opModeIsActive()) {
            while (gamepad1.a) {
                servo.setPosition(servo.getPosition()+0.05);
                TimeUnit.MILLISECONDS.sleep(250);
            }
        }
    }
}