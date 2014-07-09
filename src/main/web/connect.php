<?php

function establishDB(){
    // Details would be stored somewhere outside of web root in production
    $host = "152.78.71.57";
    $dbname = "db_dpm3g10";
    $user = "dpm3g10";
    $pass = "fuckYouKim6";

    $db = new PDO("mysql:host=$host;dbname=$dbname", $user, $pass);
    $db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    return $db;
}

?>
