package basicpayrollsystem;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class BasicPayrollSystem {

    static String empFile = "resources/MotorPH Employee Data - Employee Details.csv";
    static String attFile = "resources/MotorPH Employee Data - Attendance Record.csv";

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Username: ");
        String username = sc.nextLine();

        System.out.print("Password: ");
        String password = sc.nextLine();

        if (!password.equals("12345") ||
            !(username.equals("employee") || username.equals("payroll_staff"))) {

            System.out.println("Incorrect username and/or password.");
            return;
        }

        if (username.equals("employee")) {
            employeeMenu(sc);
        } else {
            payrollMenu(sc);
        }

        sc.close();
    }

    static void employeeMenu(Scanner sc) {

        System.out.println("\n1 Enter your employee number");
        System.out.println("2 Exit");

        int choice = sc.nextInt();
        sc.nextLine();

        if (choice == 1) {
            System.out.print("Enter Employee #: ");
            String empNo = sc.nextLine();
            displayEmployee(empNo);
        }
    }

    static void payrollMenu(Scanner sc) {

        while (true) {

            System.out.println("\n1 Process Payroll");
            System.out.println("2 Exit");

            int choice = sc.nextInt();
            sc.nextLine();

            if (choice == 2) return;

            System.out.println("\n1 One Employee");
            System.out.println("2 All Employees");
            System.out.println("3 Exit");

            int sub = sc.nextInt();
            sc.nextLine();

            if (sub == 1) {
                System.out.print("Enter Employee #: ");
                String empNo = sc.nextLine();
                processPayroll(empNo);

            } else if (sub == 2) {
                processAllEmployees();
            } else {
                return;
            }
        }
    }

    static void displayEmployee(String empNo) {

        try (BufferedReader br = new BufferedReader(new FileReader(empFile))) {

            br.readLine();
            String line;

            while ((line = br.readLine()) != null) {

                String[] data = line.split(",", -1);

                if (data[0].trim().equals(empNo)) {

                    System.out.println("Employee #: " + data[0]);
                    System.out.println("Employee Name: " + data[1] + ", " + data[2]);
                    System.out.println("Birthday: " + data[3]);
                    return;
                }
            }

            System.out.println("Employee number does not exist.");

        } catch (Exception e) {
            System.out.println("Error reading employee file.");
        }
    }

    static void processPayroll(String empNo) {

        try {

            BufferedReader empReader = new BufferedReader(new FileReader(empFile));
            String line;

            empReader.readLine();

            String lastName = "";
            String firstName = "";
            String birthday = "";
            double hourlyRate = 0;
            boolean found = false;

            while ((line = empReader.readLine()) != null) {

                String[] data = line.split(",", -1);

                if (data[0].trim().equals(empNo)) {

                    lastName = data[1].trim();
                    firstName = data[2].trim();
                    birthday = data[3].trim();

                    hourlyRate = Double.parseDouble(data[17].trim());

                    found = true;
                    break;
                }
            }

            empReader.close();

            if (!found) {
                System.out.println("Employee number does not exist.");
                return;
            }

            double cutoff1Hours = 0;
            double cutoff2Hours = 0;

            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("H:mm");

            BufferedReader attReader = new BufferedReader(new FileReader(attFile));
            attReader.readLine();

            while ((line = attReader.readLine()) != null) {

                String[] data = line.split(",", -1);

                if (!data[0].trim().equals(empNo)) continue;

                String[] dateParts = data[3].trim().split("/");

                int month = Integer.parseInt(dateParts[0]);
                int day = Integer.parseInt(dateParts[1]);

                if (month != 6) continue;

                LocalTime login = LocalTime.parse(data[4].trim(), timeFormat);
                LocalTime logout = LocalTime.parse(data[5].trim(), timeFormat);

                double hours = computeHours(login, logout);

                if (day <= 15)
                    cutoff1Hours += hours;
                else
                    cutoff2Hours += hours;
            }

            attReader.close();

            cutoff1Hours = round2(cutoff1Hours);
            cutoff2Hours = round2(cutoff2Hours);

            double gross1 = round2(cutoff1Hours * hourlyRate);
            double gross2 = round2(cutoff2Hours * hourlyRate);

            double monthlyGross = gross1 + gross2;

            double sss = 1125;
            double philhealth = 900;
            double pagibig = 100;

            double totalContributions = sss + philhealth + pagibig;

            double taxableIncome = monthlyGross - totalContributions;

            double withholdingTax = computeWithholdingTax(taxableIncome) / 2;

            double totalDeductions = round2(totalContributions + withholdingTax);

            double net1 = gross1;
            double net2 = round2(gross2 - totalDeductions);

            System.out.println("\nEmployee #: " + empNo);
            System.out.println("Employee Name: " + lastName + ", " + firstName);
            System.out.println("Birthday: " + birthday);

            System.out.println("\nCutoff Date: June 1 to 15");
            System.out.printf("Total Hours Worked: %.2f hrs\n", cutoff1Hours);
            System.out.printf("Gross Salary: %,.2f\n", gross1);
            System.out.printf("Net Salary: %,.2f\n", net1);

            System.out.println("\nCutoff Date: June 16 to 30");
            System.out.printf("Total Hours Worked: %.2f hrs\n", cutoff2Hours);
            System.out.printf("Gross Salary: %,.2f\n", gross2);

            System.out.println("Each Deduction:");
            System.out.printf("SSS: %,.2f\n", sss);
            System.out.printf("PhilHealth: %,.2f\n", philhealth);
            System.out.printf("Pag-IBIG: %,.2f\n", pagibig);
            System.out.printf("Withholding Tax: %,.2f\n", withholdingTax);

            System.out.printf("Total Deductions: %,.2f\n", totalDeductions);
            System.out.printf("Net Salary: %,.2f\n", net2);

        } catch (Exception e) {
            System.out.println("Error processing payroll.");
        }
    }

    static double computeWithholdingTax(double taxableIncome) {

        if (taxableIncome <= 20832) return 0;

        if (taxableIncome < 33333)
            return (taxableIncome - 20833) * 0.20;

        if (taxableIncome < 66667)
            return 2500 + (taxableIncome - 33333) * 0.25;

        if (taxableIncome < 166667)
            return 10833 + (taxableIncome - 66667) * 0.30;

        if (taxableIncome < 666667)
            return 40833.33 + (taxableIncome - 166667) * 0.32;

        return 200833.33 + (taxableIncome - 666667) * 0.35;
    }

    static void processAllEmployees() {

        try (BufferedReader br = new BufferedReader(new FileReader(empFile))) {

            br.readLine();
            String line;

            while ((line = br.readLine()) != null) {

                String[] data = line.split(",", -1);
                processPayroll(data[0].trim());
            }

        } catch (Exception e) {
            System.out.println("Error reading employee file.");
        }
    }

    static double computeHours(LocalTime login, LocalTime logout) {

        LocalTime start = LocalTime.of(8, 0);
        LocalTime grace = LocalTime.of(8, 10);
        LocalTime end = LocalTime.of(17, 0);

        if (!login.isAfter(grace)) login = start;
        if (login.isBefore(start)) login = start;
        if (logout.isAfter(end)) logout = end;

        double hours = Duration.between(login, logout).toMinutes() / 60.0;

        if (hours > 1) hours -= 1.0;
        if (hours > 8) hours = 8;
        if (hours < 0) hours = 0;

        return hours;
    }

    static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
