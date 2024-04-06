const mysql = require('mysql');
require("dotenv").config();
const connection = mysql.createPool({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASS,
    database:process.env.DB_NAME,
    connectionLimit : 100,
});
connection.getConnection((err)=>{
    if (err) throw err;    
    console.log("Web DB connected successfully");
});

module.exports = connection; 