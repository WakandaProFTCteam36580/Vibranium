package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp (name = "shooter")

public class shooter extends OpMode {

    DcMotor motor_sh;

    @Override
    public void init() {

        motor_sh = hardwareMap.get(DcMotor.class, "motor_sh");
        

    }

    @Override
    public void loop() {

        if(gamepad1.a) {
            motor_sh.setPower(1.0);
        }
        else{
            motor_sh.setPower(0);
        }
    }
}
