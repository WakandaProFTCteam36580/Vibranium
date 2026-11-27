package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImplEx;

@TeleOp(name = "teste catraca")
public class catraca extends OpMode {

    Servo servo_catraca;

    @Override
    public void init () {

        servo_catraca = hardwareMap.get(Servo.class,"servo_catraca");

        ((ServoImplEx) servo_catraca).setPwmRange(new PwmControl.PwmRange(500, 2500));
    }

    @Override
    public void loop() {

        if (gamepad1.a) {
            servo_catraca.setPosition(0.42);
        } else if (gamepad1.x) {
            servo_catraca.setPosition(0.75);
        }

    }
}