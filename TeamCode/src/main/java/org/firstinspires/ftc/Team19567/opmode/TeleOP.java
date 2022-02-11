package org.firstinspires.ftc.Team19567.opmode; //The "namespace" for the project is declared here. To distinguish it from other teams' packages, Team19567 is used.

//Import necessary packages/libraries

//FTC SDK packages
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

//Custom enums and classes
import org.firstinspires.ftc.Team19567.drive.SampleMecanumDriveCancelable;
import org.firstinspires.ftc.Team19567.trajectorysequence.TrajectorySequence;
import org.firstinspires.ftc.Team19567.util.AUTO_STATE;
import org.firstinspires.ftc.Team19567.util.PRESET_STATE;
import org.firstinspires.ftc.Team19567.util.Utility_Constants;
import org.firstinspires.ftc.Team19567.util.Mechanisms;
import org.firstinspires.ftc.Team19567.util.TELEOP_STATE;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;


@TeleOp(name="TeleOP", group="Dababy") //Gives the TeleOp its name in the driver station menu and categorizes it as an Iterative OpMode
public class TeleOP extends OpMode {           //Declares the class TestOPIterative, which is a child of OpMode
    //Declare OpMode members

    //ElapsedTime
    private final ElapsedTime runtime = new ElapsedTime();
    private final ElapsedTime carouselTime = new ElapsedTime();
    private final ElapsedTime carouselDebounceTime = new ElapsedTime();
    private final ElapsedTime slowModeDebounceTime = new ElapsedTime();
    private final ElapsedTime automationTimeout = new ElapsedTime();

    //Hardware
    private DcMotor leftDCFront = null;
    private DcMotor rightDCFront = null;
    private DcMotor leftDCBack = null;
    private DcMotor rightDCBack = null;
    private DcMotorEx armDC = null;
    private Servo releaseServo = null;
    private Servo balanceServo = null;
    private TouchSensor limitSwitch = null;
    private DistanceSensor distanceSensor = null;
    private RevBlinkinLedDriver blinkin = null;

    //Constants
    private final static double turnSens = Utility_Constants.TURN_SENSITIVITY;
    private final static double strafeSens = Utility_Constants.STRAFE_SENSITIVITY;
    private final static double intakeSpeed = Utility_Constants.INTAKE_SPEED;
    private final static int milliFinal = Utility_Constants.MILLI_FINAL;
    private final static int milliAcc = Utility_Constants.MILLI_ACC;
    private final static double initPower = Utility_Constants.INIT_POWER;
    private final static double accCoefficient = Utility_Constants.ACC_COEFFICIENT;
    private final static double finalPower = Utility_Constants.FINAL_POWER;
    private final static Gamepad.RumbleEffect endGameRumble = new Gamepad.RumbleEffect.Builder()
            .addStep(0.0, 1.0, 500)  //  Rumble right motor 100% for 500 mSec
            .addStep(0.0, 0.0, 300)  //  Pause for 300 mSec
            .addStep(1.0, 0.0, 250)  //  Rumble left motor 100% for 250 mSec
            .addStep(0.0, 0.0, 250)  //  Pause for 250 mSec
            .addStep(1.0, 0.0, 250)  //  Rumble left motor 100% for 250 mSec
            .build();

    private final static Gamepad.RumbleEffect boxSecured = new Gamepad.RumbleEffect.Builder()
            .addStep(0.0, 1.0, 1000)  //  Rumble right motor 100% for one whole second
            .build();

    //Variables
    private double armPos = 0;
    private double armPower = 1.0;
    private double releaseServoPos = 0.8;
    private double balanceServoPos = 0.0;
    private double acc = Utility_Constants.MAX_SENSITIVITY;
    private double carouselPower = 0.0;
    private boolean isSlowmode = false;
    private boolean isCarouselEngaged = false;
    private boolean isIntaked = false;

    //Objects
    private RevBlinkinLedDriver.BlinkinPattern blinkinPattern = RevBlinkinLedDriver.BlinkinPattern.BREATH_GRAY;
    private Mechanisms mechanisms = null;
    SampleMecanumDriveCancelable chassis;

    //Enums
    private PRESET_STATE presetState = PRESET_STATE.NO_PRESET;
    private TELEOP_STATE currentState = TELEOP_STATE.DRIVER_CONTROL;

    @Override
    public void init() {
        //Get the motors from the robot's configuration

        leftDCFront = hardwareMap.get(DcMotor.class, "leftFront");
        rightDCFront = hardwareMap.get(DcMotor.class, "rightFront");
        leftDCBack = hardwareMap.get(DcMotor.class, "leftBack");
        rightDCBack = hardwareMap.get(DcMotor.class, "rightBack");
        armDC = hardwareMap.get(DcMotorEx.class, "armDC");
        releaseServo = hardwareMap.get(Servo.class, "releaseServo");
        balanceServo = hardwareMap.get(Servo.class, "balanceServo");
        limitSwitch = hardwareMap.get(TouchSensor.class,"limitSwitch");
        blinkin = hardwareMap.get(RevBlinkinLedDriver.class, "blinkin");
        distanceSensor = hardwareMap.get(DistanceSensor.class,"distanceSensor");

        chassis = new SampleMecanumDriveCancelable(hardwareMap);
        mechanisms = new Mechanisms(hardwareMap,telemetry);

        leftDCFront.setDirection(DcMotor.Direction.FORWARD);
        rightDCFront.setDirection(DcMotor.Direction.REVERSE);
        leftDCBack.setDirection(DcMotor.Direction.FORWARD);
        rightDCBack.setDirection(DcMotor.Direction.REVERSE);

        chassis.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        mechanisms.setModes();

        telemetry.update();
    }

    @Override
    public void init_loop() {
        telemetry.addData("Status", "Awaiting Start");
        telemetry.update();
    }

    @Override
    public void start() {
        telemetry.addData("Status", "Started");
        telemetry.update();
        runtime.reset(); //Reset runtime
    }

    @Override
    public void loop() {

        //DRIVETRAIN
        chassis.update();

        switch(currentState) {
            case DRIVER_CONTROL: {
                double r = Math.hypot(gamepad1.left_stick_x, gamepad1.left_stick_y); //Gets the amount that we want to translate
                double angleDC = Math.atan2(gamepad1.left_stick_y, gamepad1.left_stick_x) - Math.PI / 4;
                if (gamepad1.triangle && slowModeDebounceTime.milliseconds() >= Utility_Constants.DEBOUNCE_TIME) {
                    isSlowmode = !isSlowmode;
                }
                if (isSlowmode) acc = Utility_Constants.SLOWMODE_MULT;
                else acc = Utility_Constants.MAX_SENSITIVITY;
                final double leftFrontSpeed = (strafeSens * r * Math.sin(angleDC) - turnSens * gamepad1.right_stick_x) * acc; //Using the math explained above, we can obtain the values we want to multiply each wheel by. acc is the variable which controls the overall multiplier of how fast we want to go.
                final double rightFrontSpeed = (strafeSens * r * Math.cos(angleDC) + turnSens * gamepad1.right_stick_x) * acc;
                final double leftBackSpeed = (strafeSens * r * Math.cos(angleDC) - turnSens * gamepad1.right_stick_x) * acc;
                final double rightBackSpeed = (strafeSens * r * Math.sin(angleDC) + turnSens * gamepad1.right_stick_x) * acc;

                leftDCFront.setPower(leftFrontSpeed); //Set all the motors to their corresponding powers/speeds
                rightDCFront.setPower(rightFrontSpeed);
                leftDCBack.setPower(leftBackSpeed);
                rightDCBack.setPower(rightBackSpeed);
                if(gamepad1.options) {
                    currentState = TELEOP_STATE.AUTOMATION_ROADRUNNER_MOVEMENT;
                    telemetry.addData("State Machine","Moved to AUTOMATION_ROADRUNNER_MOVEMENT");
                    telemetry.update();
                    TrajectorySequence warehouseSequence = chassis.trajectorySequenceBuilder(new Pose2d(0,0,Math.toRadians(0)))
                            .addSpatialMarker(new Vector2d(6,1), () -> {
                                mechanisms.rotateArm(Utility_Constants.THIRD_LEVEL_POS,0.7);
                            })
                            .lineToSplineHeading(new Pose2d(27,6, Math.toRadians(225))).build();
                    chassis.followTrajectorySequenceAsync(warehouseSequence);
                }
                break;
            }
            case AUTOMATION_ROADRUNNER_MOVEMENT: {
                if(!chassis.isBusy()) {
                    automationTimeout.reset();
                    telemetry.addData("State Machine","Moved to AUTOMATION_FLICKER");
                    telemetry.update();
                    currentState = TELEOP_STATE.AUTOMATION_FLICKER;
                }
                if(gamepad1.start) {
                    chassis.breakFollowing();
                    currentState = TELEOP_STATE.DRIVER_CONTROL;
                }
                break;
            }
            case AUTOMATION_FLICKER: {
                releaseServoPos = 0.3;
                if(automationTimeout.milliseconds() >= Utility_Constants.FLICKER_TIME) {
                    telemetry.addData("State Machine","Moved to DRIVER_CONTROL");
                    telemetry.update();
                    currentState = TELEOP_STATE.DRIVER_CONTROL;
                }
                if(gamepad1.start) {
                    chassis.breakFollowing();
                    currentState = TELEOP_STATE.DRIVER_CONTROL;
                }
                break;
            }
            default: {
                currentState = TELEOP_STATE.DRIVER_CONTROL;
            }
        }

        //INTAKE
        if(gamepad1.right_trigger > 0) mechanisms.moveIntake(intakeSpeed);
        else if(gamepad1.right_bumper) mechanisms.moveIntake(-intakeSpeed);
        else if(limitSwitch.isPressed()) mechanisms.moveIntake(0.0);
        else mechanisms.moveIntake(0.1);

        //CAROUSEL
        if(gamepad1.dpad_right || gamepad1.dpad_left && carouselDebounceTime.milliseconds() >= Utility_Constants.DEBOUNCE_TIME) {
            carouselDebounceTime.reset();
            carouselTime.reset();
            isCarouselEngaged = !isCarouselEngaged;
        }
        if(isCarouselEngaged) {
            if(carouselTime.milliseconds() <= milliFinal) carouselPower = Utility_Constants.INIT_POWER;
            else if(carouselTime.milliseconds() >= milliAcc && carouselTime.milliseconds() <= milliFinal) {
                carouselPower = Range.clip(carouselPower + carouselTime.milliseconds()/accCoefficient,initPower,finalPower);
            }
            else if(carouselTime.milliseconds() >= milliFinal) {
                carouselPower = 0.0;
            }
            else carouselPower = finalPower;
        }
        else carouselPower = 0.0;

        //ARM
        if(gamepad1.left_trigger > 0 || gamepad2.left_trigger > 0) armPos = Range.clip(armPos+gamepad1.left_trigger*4,0,1900);
        else if(gamepad1.left_trigger > 0 || gamepad2.left_trigger > 0) armPos = Range.clip(armPos+gamepad2.left_trigger*4,0,1900);
        if(gamepad1.left_bumper || gamepad2.left_bumper) armPos = Range.clip(armPos-4,0,1900);
        if(gamepad1.square || gamepad2.x) {
            presetState = PRESET_STATE.ALLIANCE_FIRST;
        }
        else if(gamepad2.y) {
            presetState = PRESET_STATE.ALLIANCE_THIRD;
        }
        else if(gamepad1.cross || gamepad2.a) {
            presetState = PRESET_STATE.ALLIANCE_THIRD;
        }
        else if(gamepad1.circle || gamepad2.b) {
            presetState = PRESET_STATE.GOING_DOWN;
        }

        //PRESET HANDLING
        switch(presetState) {
            case ALLIANCE_FIRST: {
                armPower = 1.0;
                armPos = Utility_Constants.FIRST_LEVEL_POS;
                if(armDC.getCurrentPosition() >= Utility_Constants.FIRST_LEVEL_POS-2) {
                    presetState = PRESET_STATE.NO_PRESET;
                }
                break;
            }
            case ALLIANCE_SECOND: {
                armPower = 1.0;
                armPos = Utility_Constants.SECOND_LEVEL_POS;
                if(armDC.getCurrentPosition() >= Utility_Constants.SECOND_LEVEL_POS-2) {
                    presetState = PRESET_STATE.NO_PRESET;
                }
                break;
            }
            case ALLIANCE_THIRD: {
                armPower = 1.0;
                armPos = Utility_Constants.THIRD_LEVEL_POS;
                if(armDC.getCurrentPosition() >= Utility_Constants.THIRD_LEVEL_POS-5) {
                    presetState = PRESET_STATE.NO_PRESET;
                }
                break;
            }
            case GOING_DOWN: {
                armPower = 1.0;
                armPos = 0;
                releaseServoPos = 0.8;
                if(armDC.getCurrentPosition() <= 5) {
                    presetState = PRESET_STATE.NO_PRESET;
                }
            }
            default: {
                break;
            }
        }

        //SERVOS
        if(gamepad1.dpad_down || gamepad2.dpad_down) releaseServoPos = Range.clip(releaseServoPos-0.01,releaseServo.MIN_POSITION,0.9);
        else if(gamepad1.dpad_up || gamepad2.dpad_up) releaseServoPos = Range.clip(releaseServoPos+0.01,releaseServo.MIN_POSITION,0.9);
        if(gamepad2.right_bumper) releaseServoPos = 0.3;
        if((runtime.milliseconds() >= 85000 && runtime.milliseconds() <= 90000) || (runtime.milliseconds() >= 115000)) {
            blinkinPattern = RevBlinkinLedDriver.BlinkinPattern.BEATS_PER_MINUTE_LAVA_PALETTE;
            gamepad1.runRumbleEffect(endGameRumble);
        }
        else if(presetState != PRESET_STATE.NO_PRESET) blinkinPattern = RevBlinkinLedDriver.BlinkinPattern.TWINKLES_PARTY_PALETTE;
        else if(distanceSensor.getDistance(DistanceUnit.MM) <= 80) {
            if(!isIntaked) gamepad1.runRumbleEffect(boxSecured);
            isIntaked = true;
            blinkinPattern = RevBlinkinLedDriver.BlinkinPattern.GOLD;
            telemetry.addData("Distance Sensor","Freight Detected");
        }
        else if(distanceSensor.getDistance(DistanceUnit.MM) <= 80) {
            blinkinPattern = RevBlinkinLedDriver.BlinkinPattern.GOLD;
            telemetry.addData("Distance Sensor","Freight Detected");
        }
        else {
            isIntaked = false;
            blinkinPattern = RevBlinkinLedDriver.BlinkinPattern.BREATH_GRAY;
        }
        blinkin.setPattern(blinkinPattern);

        //MOTOR SET POWER
        armDC.setTargetPosition((int) armPos);
        armDC.setPower(armPower);
        releaseServo.setPosition(releaseServoPos);
        mechanisms.rotateCarousel(carouselPower);
        mechanisms.maintainBalance();

        //TELEMETRY
        telemetry.addData("Status", "Looping");
        telemetry.addData("Runtime", runtime.toString() + " Milliseconds"); //Display the runtime
        telemetry.addData("Carousel Time(%.2f)",carouselTime.toString() + "Milliseconds"); //Display the carouselTime
        telemetry.addData("DCMotors", "armDC(%.2f)", armPos);
        telemetry.addData("Servos","releaseServoPos(%.2f), balanceServoPos(%.2f)",releaseServoPos, balanceServoPos);
        telemetry.update(); //Updates the telemetry
    }

    @Override
    public void stop() {
        telemetry.addData("Status", "Stopped");
        telemetry.addData("Runtime","Total runtime was " + runtime.toString() + " Milliseconds");
        telemetry.update();
    }
}