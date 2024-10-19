package org.firstinspires.ftc.teamcode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.classes.Drivetrain;
import org.firstinspires.ftc.teamcode.classes.Hardware;
import org.firstinspires.ftc.teamcode.classes.Lift;
import org.firstinspires.ftc.teamcode.classes.extra.GethPath;
import org.firstinspires.ftc.teamcode.classes.extra.Position;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.TouchSensor;

import static org.firstinspires.ftc.teamcode.classes.Hardware.*;
import static org.firstinspires.ftc.teamcode.classes.extra.StartingPositions.*;

@Autonomous(name = "Autonomous :)", group = "Centerstage")
public class AutonomousCenterstage extends LinearOpMode {

    //Robot parts
    Hardware RobotHardware = new Hardware();
    TouchSensor None;
    public Drivetrain CenterstageDriveTrain;
    public Lift Slides;
    public Lift Arm;
    public Lift Slider;


    //Auton data
    StartPos sPos = StartPos.UNKNOWN;
    public String StartPosString;


    public void runOpMode()
    {
        RobotHardware.StartHardware(hardwareMap);

        CenterstageDriveTrain = new Drivetrain(imu, lfront, lback, rfront, rback, lfront, lback, rfront, 1,1,1,1,1); // TODO add odometry pod stuf
        Arm = new Lift(armMotor, Lift.LiftType.SinlejointedArm, 100, 32, 0, 0.00755190904, false, 1, ArmLimit);
        Slider = new Lift(SliderMotor, Lift.LiftType.LinearSlides, 100, 32, 1, 0, true, 1, ArmLimit);



        telemetry.addData("Status: ", "Calibrating...");


        CenterstageDriveTrain.Init();
        Arm.Init();
        telemetry.update();

        try {
            File startPosFile = new File("org\\firstinspires\\ftc\\teamcode\\classes\\extra\\datafiles\\StartingPosition.txt");
            BufferedReader fileReader = new BufferedReader(new FileReader(startPosFile));
            StartPosString = fileReader.readLine();


        }
        catch (IOException e)
        {
            telemetry.addData("Error: ", e);
        }


        telemetry.addData("Status: ", "Selecting start pos...");
        telemetry.addData("StartPos: ", StartPosString);
        telemetry.update();


        //Autonomous selecting pos
        while(!gamepad1.back && !isStopRequested())
        {
            if (gamepad1.a)
                sPos = StartPos.BLUE1;

            if (gamepad1.b)
                sPos = StartPos.BLUE2;

            if (gamepad1.x)
                sPos = StartPos.RED1;

            if (gamepad1.y)
                sPos = StartPos.RED2;

            telemetry.addData("Status: ", "Selecting start pos...");
            telemetry.addData("StartPos: ", sPos);
            telemetry.update();

        }

        telemetry.addData("Status: ", "Waiting for start...");
        telemetry.update();
        // hier blijven herkennen tot start
        waitForStart();
        // Code after start is pressed------------------------------------------------------------------------------------


            while(!isStopRequested()) {


                telemetry.update();

                //if( positie, 1, 2 of 3; rijd naar de juiste plek
            }







    }

}
