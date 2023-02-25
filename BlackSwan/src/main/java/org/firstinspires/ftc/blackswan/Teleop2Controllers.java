package org.firstinspires.ftc.blackswan;
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import java.util.Date;

@Config
@TeleOp(name = "Teleop 2 controllers")
public class Teleop2Controllers extends LinearOpMode {
    private final FtcDashboard dashboard = FtcDashboard.getInstance();
    public static double openClaw = 0.50;
    public static double closeClaw = 0.2 ;
    public static double rotateClaw = 0.70;
    public static double unRotateClaw = 0;

    double manRotClaw = 0;

    @Override
    public void runOpMode() throws InterruptedException {
        DcMotor motorFrontLeft = hardwareMap.dcMotor.get("frontLeft");
        DcMotor motorBackLeft = hardwareMap.dcMotor.get("backLeft");
        DcMotor motorFrontRight = hardwareMap.dcMotor.get("frontRight");
        DcMotor motorBackRight = hardwareMap.dcMotor.get("backRight");
        DcMotor linearslide = hardwareMap.dcMotor.get("linearSlide");
        Servo clawservo = hardwareMap.servo.get("daclaw");
        Servo daSpinster = hardwareMap.servo.get("spinster");
        // Reverse the right side motors
        // Reverse left motors if you are using NeveRests
        motorFrontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        motorBackLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        linearslide.setDirection(DcMotorSimple.Direction.REVERSE);
        boolean move = false;
        linearslide.setDirection(DcMotorSimple.Direction.REVERSE);
        waitForStart();
        if (isStopRequested()) return;
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        while (opModeIsActive()) {
            double y = -gamepad1.left_stick_y; // Remember, this is reversed!
            double x = gamepad1.left_stick_x * 1.1; // Counteract imperfect strafing
            double rx = gamepad1.right_stick_x;
            // Denominator is the largest motor power (absolute value) or 1
            // This ensures all the powers maintain the same ratio, but only when
            // at least one is out of the range [-1, 1]
            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
            double frontLeftPower = (y + x + rx) / denominator;
            double backLeftPower = (y - x + rx) / denominator;
            double frontRightPower = (y - x - rx) / denominator;
            double backRightPower = (y + x - rx) / denominator;
//3250
            if (gamepad2.dpad_up) {
                if (linearslide.getCurrentPosition() < 3250) {
                    move = true;
                    linearslide.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    linearslide.setPower(0.5);
                } else {
                    linearslide.setPower(0);
                }
            } else if (gamepad2.dpad_down) {
                if (linearslide.getCurrentPosition() > 0) {
                    move = true;
                    linearslide.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    linearslide.setPower(-0.5);
                } else {
                    linearslide.setPower(0);
                }
            } else {
                if (move) {
                    move = false;
                    linearslide.setTargetPosition(linearslide.getCurrentPosition());
                    linearslide.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    linearslide.setPower(1);
                }
            }
            if (gamepad2.b) {
                clawservo.setPosition(openClaw);
            }
            if (gamepad2.a) {
                clawservo.setPosition(closeClaw);
            }
            if (gamepad2.left_bumper && linearslide.getCurrentPosition() > 1000){
//                daSpinster.setPosition(rotateClaw);

                  manRotClaw+=0.0035;
            }
            if (gamepad2.right_bumper && linearslide.getCurrentPosition() > 1000){
//                daSpinster.setPosition(unRotateClaw);

                manRotClaw-=0.0035;
            }

            daSpinster.setPosition(manRotClaw);


            telemetry.addData("GamepadX", x);
            telemetry.addData("GamepadY", y);
            telemetry.addData("armpos", clawservo.getPosition());
            telemetry.addData("servopos", clawservo.getPosition());
            telemetry.addData("Linear Slide Position", linearslide.getCurrentPosition());
            telemetry.update();
            motorFrontLeft.setPower(frontLeftPower);
            motorBackLeft.setPower(backLeftPower);
            motorFrontRight.setPower(frontRightPower);
            motorBackRight.setPower(backRightPower);
        }
    }
}