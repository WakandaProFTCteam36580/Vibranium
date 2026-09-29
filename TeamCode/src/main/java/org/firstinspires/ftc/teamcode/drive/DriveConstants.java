package org.firstinspires.ftc.teamcode.drive;

public class DriveConstants {

    // Especificações dos motores (ex: goBILDA Yellowjacket 5203 / 312 RPM)
    public static final double TICKS_PER_REV = 537.7;
    public static final double MAX_RPM = 312;

    // Constantes de Feedforward do RoadRunner
    public static double kV = 0.015;
    public static double kA = 0.0001;
    public static double kStatic = 0.0;

    // Dimensões mecânicas em polegadas
    public static double WHEEL_RADIUS = 1.88976; // Rodas de 96mm (raio 48mm = 1.88976 pol)
    public static double GEAR_RATIO = 1.0;        // 1.0 para tração direta
    public static double TRACK_WIDTH = 14.5;      // Distância entre rodas da esquerda e direita (pol)

    // Restrições de velocidade e aceleração do robô
    public static double MAX_VEL = 50.0;
    public static double MAX_ACCEL = 50.0;
    public static double MAX_ANG_VEL = Math.toRadians(180);
    public static double MAX_ANG_ACCEL = Math.toRadians(180);

    public static double encoderTicksToInches(double ticks) {
        return WHEEL_RADIUS * 2 * Math.PI * GEAR_RATIO * ticks / TICKS_PER_REV;
    }
}