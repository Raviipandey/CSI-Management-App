const mysql = require('mysql');

const connection = mysql.createConnection({
    host: '128.199.23.207',
    user: 'csi',
    password: 'csi',
    database: 'csiApp2022'
});

connection.connect(function(err) {
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