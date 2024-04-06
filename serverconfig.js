const mysql = require('mysql');
require("dotenv").config();
let connection = require('./Database/dbConnect');
const connection1 = mysql.createConnection({

    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASS,
    database:process.env.DB_NAME,
});

connection.getConnection(function(err) {
    if (err) {
        console.log('Not Connected to MySql! dbConfig.js');
    } else {
        console.log('Connected To Mysql! dbConfig.js');
    }
});


const server_url = 'https://csiapp.dbit.in';

module.exports = {
    connection: connection,
    server_url: server_url
};