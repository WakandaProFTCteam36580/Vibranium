package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "teste catraca")
public class catraca extends OpMode {

    Servo servo_catraca;

    @Override
    public void init () {

        servo_catraca = hardwareMap.get(Servo.class,"servo_catraca");

        servo_catraca.setDirection(Servo.Direction.REVERSE);
    }

    @Override
    public void loop() {

        if (gamepad1.a) {
            servo_catraca.setPosition(1.0); // Posição máxima (ex: aberto)
        } else if (gamepad1.b) {
            servo_catraca.setPosition(0.0); // Posição mínima (ex: fechado)
        } else if (gamepad1.x) {
            servo_catraca.setPosition(0.5); // Posição intermediária (opcional)
        }

    }
}


//cursinho que eu trabalhe as duas habilidades