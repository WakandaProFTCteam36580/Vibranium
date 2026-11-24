package org.firstinspires.ftc.teamcode.drive;

import androidx.annotation.NonNull;

import com.acmerobotics.roadrunner.control.PIDCoefficients;
import com.acmerobotics.roadrunner.drive.DriveSignal;
import com.acmerobotics.roadrunner.drive.TankDrive;
import com.acmerobotics.roadrunner.followers.TankPIDVAFollower;
import com.acmerobotics.roadrunner.followers.TrajectoryFollower;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.acmerobotics.roadrunner.trajectory.TrajectoryBuilder;
import com.acmerobotics.roadrunner.trajectory.constraints.AngularVelocityConstraint;
import com.acmerobotics.roadrunner.trajectory.constraints.MinVelocityConstraint;
import com.acmerobotics.roadrunner.trajectory.constraints.ProfileAccelerationConstraint;
import com.acmerobotics.roadrunner.trajectory.constraints.TankVelocityConstraint;
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryAccelerationConstraint;
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryVelocityConstraint;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.CRServoImplEx;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import java.util.Arrays;
import java.util.List;

public class SampleTankDrive extends TankDrive {

    public static PIDCoefficients AXIAL_PID = new PIDCoefficients(0, 0, 0);
    public static PIDCoefficients CROSS_TRACK_PID = new PIDCoefficients(0, 0, 0);
    public static PIDCoefficients HEADING_PID = new PIDCoefficients(0, 0, 0);

    public static double VX_WEIGHT = 1;
    public static double OMEGA_WEIGHT = 1;

    private TrajectoryFollower follower;

    // Motores da Tração
    private DcMotorEx leftMotor;
    private DcMotorEx rightMotor;
    private List<DcMotorEx> motors;

    // --- MOTORES E SERVOS ADICIONAIS ---
    public DcMotorEx motor_it;   // Porta 2
    public DcMotorEx motor_sh;   // Porta 3

    public CRServo servoit_esq;  // Porta Servo 0
    public CRServo servoit_dir;  // Porta Servo 1
    public CRServo servo_lift;   // Porta Servo 2

    // --- GIROSCÓPIO INTEGRADO (IMU) ---
    public IMU imu;

    private TrajectoryVelocityConstraint VEL_CONSTRAINT;
    private TrajectoryAccelerationConstraint ACCEL_CONSTRAINT;

    public SampleTankDrive(HardwareMap hardwareMap) {
        super(DriveConstants.kV, DriveConstants.kA, DriveConstants.kStatic, DriveConstants.TRACK_WIDTH);

        VEL_CONSTRAINT = getVelocityConstraint(DriveConstants.MAX_VEL, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH);
        ACCEL_CONSTRAINT = getAccelerationConstraint(DriveConstants.MAX_ACCEL);

        follower = new TankPIDVAFollower(AXIAL_PID, CROSS_TRACK_PID,
                new Pose2d(VX_WEIGHT, 0, HEADING_PID.kP),
                0.5);

        // Mapeamento dos Motores de Tração
        leftMotor = hardwareMap.get(DcMotorEx.class, "motor_esq");
        rightMotor = hardwareMap.get(DcMotorEx.class, "motor_dir");

        motors = Arrays.asList(leftMotor, rightMotor);

        for (DcMotorEx motor : motors) {
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }

        leftMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        rightMotor.setDirection(DcMotorSimple.Direction.REVERSE);

        // Mapeamento dos Mecanismos
        motor_it = hardwareMap.get(DcMotorEx.class, "motor_it");
        motor_sh = hardwareMap.get(DcMotorEx.class, "motor_sh");

        servo_lift = hardwareMap.get(CRServo.class, "servo_lift");

        motor_it.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motor_sh.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        // --- INICIALIZAÇÃO DO GIROSCÓPIO DA CONTROL HUB ---
        imu = hardwareMap.get(IMU.class, "imu");

        // Ajuste a orientação física de montagem da sua Control Hub no chassis
        IMU.Parameters parameters = new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD));

        imu.initialize(parameters);
        imu.resetYaw();
    }

    /**
     * Gira o robô por graus reais medidos pelo giroscópio (IMU).
     * @param anguloAlvo Graus desejados (Positivo = Esquerda / Negativo = Direita)
     * @param potencia Potência de rotação dos motores (ex: 0.4)
     */
    public void girarPorAngulo(double anguloAlvo, double potencia) {
        imu.resetYaw(); // Zera o ângulo antes do giro

        // Define a direção com base no sinal do alvo
        double potEsq = (anguloAlvo > 0) ? -potencia : potencia;
        double potDir = (anguloAlvo > 0) ? potencia : -potencia;

        setMotorPowers(potEsq, potDir);

        // Define uma margem de tolerância (ex: 2 graus de margem)
        double tolerancia = 2.0;

        if (anguloAlvo > 0) {
            // Giro para a Esquerda (o ângulo atual aumenta em valores positivos)
            while (getAnguloAtual() < (anguloAlvo - tolerancia)) {
                // O ciclo continua vazio, MAS agora a condição relê o sensor a cada iteração!
                // Nota: para garantir que o FTC processa o ciclo sem congelar,
                // podes adicionar um pequeno yield se necessário, mas ler o getAnguloAtual() já força a atualização.
            }
        } else {
            // Giro para a Direita (o ângulo atual desce em valores negativos)
            while (getAnguloAtual() > (anguloAlvo + tolerancia)) {
                // Continua a rodar até atingir o limite com tolerância
            }
        }

        setMotorPowers(0, 0); // Trava obrigatoriamente os motores ao atingir o alvo
    }

    /**
     * Retorna o ângulo Yaw atual em graus (-180° a +180°).
     */
    public double getAnguloAtual() {
        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
    }

    public TrajectoryBuilder trajectoryBuilder(Pose2d startPose) {
        return new TrajectoryBuilder(startPose, VEL_CONSTRAINT, ACCEL_CONSTRAINT);
    }

    public TrajectoryBuilder trajectoryBuilder(Pose2d startPose, boolean reversed) {
        return new TrajectoryBuilder(startPose, reversed, VEL_CONSTRAINT, ACCEL_CONSTRAINT);
    }

    public TrajectoryBuilder trajectoryBuilder(Pose2d startPose, double startHeading) {
        return new TrajectoryBuilder(startPose, startHeading, VEL_CONSTRAINT, ACCEL_CONSTRAINT);
    }

    public void followTrajectory(Trajectory trajectory) {
        follower.followTrajectory(trajectory);
        while (!Thread.currentThread().isInterrupted() && follower.isFollowing()) {
            Pose2d currentPose = getPoseEstimate();
            DriveSignal signal = follower.update(currentPose, getPoseVelocity());
            setDriveSignal(signal);
            updatePoseEstimate();
        }
    }

    @NonNull
    @Override
    public List<Double> getWheelPositions() {
        return Arrays.asList(
                (double) leftMotor.getCurrentPosition(),
                (double) rightMotor.getCurrentPosition()
        );
    }

    @Override
    public void setMotorPowers(double v, double v1) {
        leftMotor.setPower(v);
        rightMotor.setPower(v1);
    }

    @Override
    public double getRawExternalHeading() {
        return 0.0;
    }

    public static TrajectoryVelocityConstraint getVelocityConstraint(double maxMaxVel, double maxAngularVel, double trackWidth) {
        return new MinVelocityConstraint(Arrays.asList(
                new AngularVelocityConstraint(maxAngularVel),
                new TankVelocityConstraint(maxMaxVel, trackWidth)
        ));
    }

    public static TrajectoryAccelerationConstraint getAccelerationConstraint(double maxAccel) {
        return new ProfileAccelerationConstraint(maxAccel);
    }
}