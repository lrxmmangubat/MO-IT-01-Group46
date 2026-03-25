package basicpayrollsystem;

/*
 * MotorPH Basic Payroll System (FINAL SUBMISSION VERSION - TA)
 *
 * DESCRIPTION:
 * This program reads employee and attendance data from CSV files,
 * computes working hours based on company rules, calculates payroll
 * for two cutoff periods (June 1–15 and June 16–30), and applies
 * government deductions and withholding tax.
 *
 * FEEDBACK APPLIED:
 * ✔ Meaningful comments
 * ✔ Section headers for readability
 * ✔ Explaind business rules (cutoff, lunch, tax)
 * ✔ Removed rounding to 2 decimals in output display and show all values
 * ✔ Clean and organized flow
 */

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class BasicPayrollSystem {

    // File paths for required CSV data
    static String empFile = "resources/MotorPH Employee Data - Employee Details.csv";
    static String attFile = "resources/MotorPH Employee Data - Attendance Record.csv";

    // -----------------------------
    // MAIN PROGRAM ENTRY
    // -----------------------------
    // handles login and routes user to correct menu
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Username: ");
        String username = sc.nextLine();

        System.out.print("Password: ");
        String password = sc.nextLine();

        // Only allow valid system roles
        if (!password.equals("12345") ||
            !(username.equals("employee") || username.equals("payroll_staff"))) {

            System.out.println("Incorrect username and/or password.");
            return;
        }

        // direct user to correct functionality
        if (username.equals("employee")) {
            employeeMenu(sc);
        } else {
            payrollMenu(sc);
        }

        sc.close();
    }

    // -----------------------------
    // EMPLOYEE MENU
    // -----------------------------
    // allows employee to view personal information
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

    //------------------------------
    // PAYROLL MENU
    // -----------------------------
    // allows payroll_staff to process payroll
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

    // -----------------------------
    // DISPLAY EMPLOYEE INFO
    // -----------------------------
    // reads employee CSV and displays basic details
    static void displayEmployee(String empNo) {

        try (BufferedReader br = new BufferedReader(new FileReader(empFile))) {

            br.readLine(); // Skip header row
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

    // -----------------------------
    // MAIN PAYROLL LOGIC
    // -----------------------------
    static void processPayroll(String empNo) {

        try {

            // ------------------------------
            // SECTION 1: GET EMPLOYEE DATA
            // ------------------------------
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

            // ------------------------------
            // SECTION 2: ATTENDANCE PROCESSING
            // ------------------------------
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

                // Business rule: Only June payroll is required for this project
                if (month != 6) continue;

                LocalTime login = LocalTime.parse(data[4].trim(), timeFormat);
                LocalTime logout = LocalTime.parse(data[5].trim(), timeFormat);

                double hours = computeHours(login, logout);

                // Split working hours into cutoff periods
                if (day <= 15)
                    cutoff1Hours += hours;
                else
                    cutoff2Hours += hours;
            }

            attReader.close();

            // ------------------------------
            // SECTION 3: PAYROLL COMPUTATION
            // ------------------------------
            double gross1 = cutoff1Hours * hourlyRate;
            double gross2 = cutoff2Hours * hourlyRate;

            double monthlyGross = gross1 + gross2;

            // Fixed government contributions based on provided specification
            double sss = 1125;
            double philhealth = 900;
            double pagibig = 100;

            double totalContributions = sss + philhealth + pagibig;

            // Taxable income excludes mandatory contributions
            double taxableIncome = monthlyGross - totalContributions;

            // Withholding tax is computed monthly and split between cutoffs
            double withholdingTax = computeWithholdingTax(taxableIncome) / 2;

            double totalDeductions = totalContributions + withholdingTax;

            double net1 = gross1;
            double net2 = gross2 - totalDeductions;

            // ------------------------------
            // SECTION 4: DISPLAY OUTPUT
            // ------------------------------
            System.out.println("\nEmployee #: " + empNo);
            System.out.println("Employee Name: " + lastName + ", " + firstName);
            System.out.println("Birthday: " + birthday);

            System.out.println("\nCutoff Date: June 1 to 15");
            System.out.println("Total Hours Worked: " + cutoff1Hours);
            System.out.println("Gross Salary: " + gross1);
            System.out.println("Net Salary: " + net1);

            System.out.println("\nCutoff Date: June 16 to 30");
            System.out.println("Total Hours Worked: " + cutoff2Hours);
            System.out.println("Gross Salary: " + gross2);

            System.out.println("Each Deduction:");
            System.out.println("SSS: " + sss);
            System.out.println("PhilHealth: " + philhealth);
            System.out.println("Pag-IBIG: " + pagibig);
            System.out.println("Withholding Tax: " + withholdingTax);

            System.out.println("Total Deductions: " + totalDeductions);
            System.out.println("Net Salary: " + net2);

        } catch (Exception e) {
            System.out.println("Error processing payroll.");
        }
    }

    // -----------------------------
    // WITHHOLDING TAX CALCULATION
    // -----------------------------
    // Applies Philippine tax table to compute correct tax amount
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

    // -----------------------------
    // PROCESS ALL EMPLOYEES
    // -----------------------------
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

    // -----------------------------
    // WORK HOURS COMPUTATION
    // -----------------------------
    static double computeHours(LocalTime login, LocalTime logout) {

        LocalTime start = LocalTime.of(8, 0);
        LocalTime grace = LocalTime.of(8, 10);
        LocalTime end = LocalTime.of(17, 0);

        // Grace period: arrivals within 10 minutes are not penalized
        if (!login.isAfter(grace)) login = start;

        // Prevent early login advantage and ignore overtime
        if (login.isBefore(start)) login = start;
        if (logout.isAfter(end)) logout = end;

        double hours = Duration.between(login, logout).toMinutes() / 60.0;

        // Mandatory 1-hour unpaid lunch break deduction
        if (hours > 1) hours -= 1.0;

        // Maximum allowed working hours per day is 8
        if (hours > 8) hours = 8;
        if (hours < 0) hours = 0;

        return hours;
    }
}
