const connection = require("../../Database/dbConnect");
const session = require("express-session");
const path = require("path");
const { request } = require("http");
const { response } = require("express");
const multer = require('multer');
const csv = require('fast-csv');
const fs = require('fs');
const crypto = require('crypto');
const admin = require('firebase-admin');
const upload = multer({ dest: 'server_uploads/csv_upload' });
var serviceAccount = require('../../firebase/ServiceAccount.json');

const roleMapping = {
    'Chairperson': 3,
    'chairperson' : 3,
    'Vice Chairperson': 4,
    'vice chairperson': 4,
    'Event Head': 5,
    'event head':5,
    'Technical Head': 6,
    'Tech Head': 6,
    'technical head': 6,
    'Web Development Head': 7,
    'Wed-d Head':7,
    'web development head':7,
    'web-d head':7,
    'Public Relations Head': 8,
    'public relations head':8,
    'PR Head':8,
    'pr head':8,
    'Pr Head':8,
    'Creative Head': 9,
    'creative head':9,
    'Secretary': 10,
    'secretrary':10,
    'Documentation Head': 11,
    'documentation head':11,
    'Test Login': 12,
    'test login':12
};

const capitalize = (str) => str.toLowerCase().replace(/\b\w/g, char => char.toUpperCase());

const convertGender = gender => gender.toLowerCase().startsWith('f') ? 'female' : 'male';

const convertAcademicYear = acad => {
    const yearMatch = acad.match(/20(\d{2})-20(\d{2})/);
    return yearMatch ? `${yearMatch[1]}-${yearMatch[2]}` : acad;
};

const convertDate = dateStr => {
    console.log(`Original date string received: '${dateStr}'`);
    
    // Normalize and parse the date string by replacing "/" with "-" and ensuring month and day are zero-padded
    const normalizedDate = dateStr.replace(/\//g, "-").split("-").map((part, index) => {
        // Zero-pad month and day parts (index 0 or 1) if necessary
        return (index === 0 || index === 1) && part.length === 1 ? `0${part}` : part;
    }).join("-");

    console.log(`Normalized date string: '${normalizedDate}'`);

    // Detect the format and rearrange the parts if needed
    let [part1, part2, part3] = normalizedDate.split("-");
    if (part3.length === 4) {
        // If the year is the last part, it might be DD-MM-YYYY or MM-DD-YYYY; we'll treat it as DD-MM-YYYY
        const formattedDate = `${part3}-${part2}-${part1}`; // Rearrange to YYYY-MM-DD
        const date = new Date(formattedDate);
        console.log(`Reformatted date (assuming DD-MM-YYYY): '${formattedDate}', isValid: ${!isNaN(date.getTime())}`);
        return !isNaN(date.getTime()) ? formattedDate : '';
    } else if (part1.length === 4) {
        // If the first part is the year (YYYY-MM-DD or YYYY-DD-MM), check if it's a valid date as is
        const formattedDate = `${part1}-${part2}-${part3}`;
        const date = new Date(formattedDate);
        console.log(`Reformatted date (assuming YYYY-MM-DD): '${formattedDate}', isValid: ${!isNaN(date.getTime())}`);
        return !isNaN(date.getTime()) ? formattedDate : '';
    }

    console.log(`Date conversion failed for: '${dateStr}'`);
    return '';
};





const validateAndFormatEmail = email => {
    return email.toLowerCase();
};

const validateAndFormatPhone = phone => {
    const cleanedPhone = phone.replace(/\D/g, '');
    return cleanedPhone.length === 10 ? cleanedPhone : null;
};

const convertClassName = (className) => {
    const classMapping = {
        'final year': 'BE',
        'third year': 'TE',
        'second year': 'SE',
    };
    const normalizedClassName = className.trim().toLowerCase();
    const convertedClassName = classMapping[normalizedClassName] || className;
    return convertedClassName.toUpperCase();
};

const convertBranchName = (branchName) => {
    return branchName.trim().toUpperCase();
};

function hashPassword(password) {
    return crypto.createHash('md5').update(password).digest('hex');
}



function validateRow(row) {
    const errors = [];
    // Define regex for email validation
    const emailRegex = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/;
    // Define regex for mobile number validation (assuming Indian mobile numbers for example)
    const mobileRegex = /^[6-9]\d{9}$/;
     // Define regex for roll number validation (one or two digits)
     const rollNoRegex = /^\d{1,2}$/;

    // Add checks for each required field
    if (!row.role || row.role.trim() === '') errors.push('Role is missing or invalid.');
    
    if (!row.first_name || row.first_name.trim() === '') errors.push('First name is missing.');
    if (!row.last_name || row.last_name.trim() === '') errors.push('Last name is missing.');
    if (!row.roll_no || row.roll_no.trim() === '') errors.push('Roll number is missing.');
    else if (!rollNoRegex.test(row.roll_no)) errors.push(`Roll number must be one or two digits: ${row.roll_no}`);
    if (!row.mob_no || row.mob_no.trim() === '') errors.push('Mobile number is missing.');
    else if (!mobileRegex.test(row.mob_no)) errors.push(`Invalid mobile number format: ${row.mob_no}`);
    if (!row.email || row.email.trim() === '') errors.push('Email is missing.');
    else if (!emailRegex.test(row.email)) errors.push(`Invalid email format: ${row.email}`);
    if (!row.password || row.password.trim() === '') errors.push('Password is missing.');
    if (!row.branch || row.branch.trim() === '') errors.push('Branch is missing.');
    if (!row.class_name || row.class_name.trim() === '') errors.push('Class name is missing.');
    if (!row.acad || row.acad.trim() === '') errors.push('Academic year is missing.');
    if (!row.gender || row.gender.trim() === '') errors.push('Gender is missing.');
    if (!row.mem_strt || row.mem_strt.trim() === '') errors.push('Membership start date is missing.');
    else if (convertDate(row.mem_strt) === '') errors.push(`Invalid membership start date format: ${row.mem_strt}`);
    if (!row.mem_end || row.mem_end.trim() === '') errors.push('Membership end date is missing.');
    else if (convertDate(row.mem_end) === '') errors.push(`Invalid membership end date format: ${row.mem_end}`);

     // Convert dates from strings to Date objects
     const memStart = new Date(convertDate(row.mem_strt));
     const memEnd = new Date(convertDate(row.mem_end));
 
     // Check if conversion was successful
     if (isNaN(memStart.getTime()) || isNaN(memEnd.getTime())) {
         errors.push('One or both dates are invalid.');
     } else {
         // Check that mem_end is after mem_start and at least in the next calendar year
         if (memEnd <= memStart) {
             errors.push('Membership end date must be after the start date.');
         } else {
             // Extract years from dates
             const startYear = memStart.getFullYear();
             const endYear = memEnd.getFullYear();
 
             // Ensure mem_end year is greater than mem_start year
             if (endYear <= startYear) {
                 errors.push('Membership end date must be in a year after the start date.');
             }
         }
     }

    return errors;
}


module.exports = {
    get: (request, response) => {
        var session = request.session;
        var FeedbackPath = path.join(__dirname, "..", "..", "views", "pages", "addmembers.ejs");
        if (session.userid != null && (session.userrole == 1)) {
            response.render(FeedbackPath, { role: session.userrole, rolename: session.rolename });
        } else {
            // Redirect the user or send an error message if they don't have the right role
            // res.status(403).send('Access Denied: You do not have permission to view this page.');
            response.redirect('/error?message=access-denied');
        }
    },
    addmembers: (request, response) => {
        console.log("Received request", request.body);
        const { role, first_name, last_name, roll_no, mob_no, email, password, branch, class_name, mem_strt, mem_end, acad } = request.body;
        
        // Encrypting password using MD5
        const encryptedPassword = crypto.createHash('md5').update(password).digest('hex');

        const userRecord = admin.auth().createUser({
            email: email,
            password: encryptedPassword
        });

        // Step 1: Insert data into core_details to get core_id
        connection.query(
            'INSERT INTO csiApp2022.core_details (core_role_id, core_en_fname, core_en_lname, core_rollno, core_mobileno, core_email, core_pwd, core_branch, core_class, core_acad_year, gender) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
            [role, first_name, last_name, roll_no, mob_no, email, encryptedPassword, branch, class_name, acad, request.body.gender],
            (error, result) => {
                if (error) {
                    console.error('Error inserting data into the core_details table:', error.message);
                    response.status(500).json({ message: 'Internal Server Error', error: error.message });
                } else {
                    // Step 2: Use the obtained core_id to insert data into core_membership
                    const core_id = result.insertId; // Assuming the auto-incremented primary key is core_id
                    connection.query(
                        'INSERT INTO csiApp2022.core_membership (core_id, membership_start_date, membership_end_date) VALUES (?, ?, ?)',
                        [core_id, mem_strt, mem_end],
                        (membershipError, membershipResult) => {
                            if (membershipError) {
                                console.error('Error inserting data into the core_membership table:', membershipError.message);
                                response.status(500).json({ message: 'Internal Server Error', error: membershipError.message });
                            } else {
                                // Respond with success message
                                response.json({ message: 'Member added successfully', coreId: core_id });
                            }
                        }
                    );
                }
            }
        );
    },
    
    countMembers: (request, response) => {
        console.log("Received request for /countMembers");

        connection.query("SELECT COUNT(*) AS count FROM core_details", function (error, results, fields) {
            if (error) {
                console.error("Error fetching count of core", error);
                response.status(500).json({message: "Error fetching count of core", error: error});
                return;
            }
            console.log("Query executed successfully.");
            
            if (results.length > 0) {
                console.log("Count of core:", results[0].count);
                response.json({
                    count: results[0].count
                });
            } else {
                console.log("No results found. Returning count as 0.");
                response.json({
                    count: 0
                });
            }
        });
    },
    fetchCoreMembers: (request, response) => {
        const year = request.params.year; // Assuming you have defined ":year" as a route parameter
        // Validate the year format to prevent SQL injection and ensure it's a valid year format
        if (!/^\d{2}-\d{2}$/.test(year)) {
            console.error("Invalid year format received", year);
            response.status(400).json({message: "Invalid year format"});
            return;
        }
    
        console.log(`Received request for /members/${year}`);
    
        connection.query("SELECT cd.core_en_fname, crm.role_name FROM core_details AS cd JOIN core_role_master AS crm ON cd.core_role_id = crm.role_id WHERE core_acad_year = ?", [year], function (error, results, fields) {
            if (error) {
                console.error(`Error fetching core members for ${year}`, error);
                response.status(500).json({message: `Error fetching core members for ${year}`, error: error});
                return;
            }
            console.log(`Query executed successfully for ${year}.`);
    
            if (results.length > 0) {
                console.log(`Members fetched for ${year}:`, results.length);
                response.json({
                    year: year,
                    members: results.map(row => row.core_en_fname),
                    role : results.map(row=>row.role_name)
                });
            } else {
                console.log(`No results found for ${year}. Returning empty list.`);
                response.json({
                    year: year,
                    members: []
                });
            }
        });
    },
    uploadCSV: (request, response) => {
        upload.single('csvFile')(request, response, function (err) {
            if (err instanceof multer.MulterError) {
                return response.status(500).json({ success: false, message: err.message });
            } else if (err) {
                return response.status(500).json({ success: false, message: err.message });
            }
    
            let validationErrors = [];
            const fileRows = [];
    
            fs.createReadStream(request.file.path)
                .pipe(csv.parse({ headers: true }))
                .on('error', error => console.error(error))
                .on('data', row => {
                    // Perform initial conversions
                    row.role = capitalize(row.role); // Capitalize the role
                    row.first_name = capitalize(row.first_name); // Capitalize first name
                    row.last_name = capitalize(row.last_name); // Capitalize last name
                    row.class_name = convertClassName(row.class_name);
                    row.branch = convertBranchName(row.branch);
                    row.gender = convertGender(row.gender);
                    row.acad = convertAcademicYear(row.acad);
                    row.mem_strt = convertDate(row.mem_strt);
                    row.mem_end = convertDate(row.mem_end);
                    row.email = validateAndFormatEmail(row.email);
                    row.mob_no = validateAndFormatPhone(row.mob_no);
                     // Hash the password
    const originalPassword = row.password;
    const hashedPassword = hashPassword(originalPassword);
    row.password = hashedPassword; // Update the password in the row to its hashed version
                    
    
                    // Validate row and accumulate errors
                    const rowErrors = validateRow(row);
                    if (rowErrors.length > 0) {
                        validationErrors = validationErrors.concat(rowErrors); // Accumulate errors from all rows
                    } else {
                        row.role = roleMapping[row.role] || row.role; // Map role to ID after initial conversion
                        fileRows.push(row); // Add the processed row to fileRows if no errors
                    }
                })
                .on('end', () => {
                    if (validationErrors.length > 0) {
                        fs.unlinkSync(request.file.path); // Cleanup uploaded file
                        // Respond with an error status and a message detailing the issues
                        return response.status(400).json({
                            success: false,
                            message: 'Validation errors in uploaded CSV file',
                            errors: validationErrors // Send all errors to the client
                        });
                    }
    
                    // Process the validated and converted fileRows
                   // Process the fileRows array
                   fileRows.forEach(row => {
                    const { role, first_name, last_name, roll_no, mob_no, email, password, branch, class_name, mem_strt, mem_end, acad, gender } = row;

                    const userRecord = admin.auth().createUser({
                        email: email,
                        password: password
                    });
                    
                    // Step 1: Insert data into core_details to get core_id
                    connection.query(
                        'INSERT INTO csiApp2022.core_details (core_role_id, core_en_fname, core_en_lname, core_rollno, core_mobileno, core_email, core_pwd, core_branch, core_class, core_acad_year, gender) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
                        [role, first_name, last_name, roll_no, mob_no, email, password, branch, class_name, acad, gender],
                        (error, result) => {
                            if (error) {
                                console.error('Error inserting data into the core_details table:', error);
                            } else {
                                // Step 2: Use the obtained core_id to insert data into core_membership
                                const core_id = result.insertId;
                                connection.query(
                                    'INSERT INTO csiApp2022.core_membership (core_id, membership_start_date, membership_end_date) VALUES (?, ?, ?)',
                                    [core_id, mem_strt, mem_end],
                                    (membershipError) => {
                                        if (membershipError) {
                                            console.error('Error inserting data into the core_membership table:', membershipError);
                                        }
                                    }
                                );
                            }
                        }
                    );
                });
    
                    fs.unlinkSync(request.file.path); // Optionally delete the file after processing
                    response.status(200).json({ success: true, message: 'CSV file has been processed successfully' });
                });
        });
    },
};