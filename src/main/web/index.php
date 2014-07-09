<!DOCTYPE html>
<html>
<head>
    <title>Trial Viewer</title>
</head>
<body>
<h1>Trials</h1>
<?php
    require("connect.php");

    try{
        $db = establishDB();

        $fetchQuery = $db->query("SELECT * FROM INTERVENTION");
        $fetchQuery->setFetchMode(PDO::FETCH_ASSOC); // Index table columns by name

        echo "<ul style=\"list-style:none;padding-left:0px;\">";
        while($trial = $fetchQuery->fetch()){
            echo "<li>Trial " . $trial['id'] . ": " . $trial['trial_name'];
            echo "<br /><a href=\"trial.php?id=" . $trial['id'] . "\">Trial details</a></li>";
        }
        echo "</ul>";
        
        $db = null;
    } catch(PDOException $e){
        echo $e->getMessage();
        $db = null;
    }
?>
</body>
</html>
