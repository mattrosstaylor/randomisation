<!DOCTYPE html>
<html>
<head>
<?php
    require("connect.php");

    $db = establishDB();

    $id = htmlspecialchars($_GET['id']);

    try{
        $trialQuery = $db->prepare("SELECT * FROM INTERVENTION WHERE id=:id");
        $trialQuery->setFetchMode(PDO::FETCH_ASSOC);
        $trialQuery->execute(array(":id" => $id));
        $trialInfo = $trialQuery->fetch();

        if(!$trialInfo)
            return;

        $idArray = array(":id" => $id);

        $attrQuery = $db->prepare("SELECT * FROM ATTRIBUTE WHERE trial_definition_id=:id");
        $attrQuery->setFetchMode(PDO::FETCH_ASSOC);
        $attrQuery->execute($idArray);
        $attributes = $attrQuery->fetchAll();

        $armQuery = $db->prepare("SELECT * FROM TREATMENT WHERE trial_definition_id=:id");
        $armQuery->setFetchMode(PDO::FETCH_ASSOC);
        $armQuery->execute($idArray);
        $arms = $armQuery->fetchAll();

        $paramQuery = $db->prepare("SELECT * FROM INTERVENTION_PARAMS WHERE trial_definition_id=:id");
        $paramQuery->setFetchmode(PDO::FETCH_ASSOC);
        $paramQuery->execute($idArray);
        $params = $paramQuery->fetchAll();

        $countSQL = "SELECT COUNT(*) FROM PARTICIPANT,TREATMENT WHERE PARTICIPANT.treatment_id=TREATMENT.id AND TREATMENT.trial_definition_id=:id";
        $statsQuery = $db->prepare($countSQL);
        $statsQuery->execute($idArray);
        $totalAllocations = $statsQuery->fetchColumn();

    } catch(PDOException $e){
        echo $e->getMessage();
        $db = null;
    }

?>
    <title><?php echo (($trialInfo) ? "Trial Details: " . $trialInfo['trial_name'] : "Trial Not Found"); ?></title>
</head>
<body>
<?php
    if(!$trialInfo){
        echo "No trial found with ID: " . $id;
        return;
    }

    echo "<h1>Trial Details</h1>\n";

    // Basic trial information
    echo "<ul style=\"list-style:none;padding-left:0px;\">\n";
    echo "\t<li>Trial ID: " . $trialInfo['id'] . "</li>\n";
    echo "\t<li>Trial name: " . $trialInfo['trial_name'] . "</li>\n";
    echo "\t<li>Allocation method: " . $trialInfo['strategy_id'] . "</li><br />\n";

    // Trial parameters

    if($params){
        echo "\t<li>Parameters:</li>\n";
        echo "\t<ul style=\"list-style:none\">\n";
        foreach($params as $param)
            echo "\t\t<li>" . $param['name'] . ": " . $param['value'] . "</li>\n";
        echo "\t</ul><br />\n";
    }

    // Treatment arms
    if($arms){
        echo "\t<li>Treatment Arms:<br />\n";
        echo "\t<ul style=\"list-style:none\">\n";
        foreach($arms as $arm){
            echo "\t\t<li>" . $arm['name'] . ", Weight: " . $arm['weight'];
            if($arm['participant_limit'] == "true")
                echo ", Maximum of " . $arm['max_participants'] . " participants";
            echo "</li>\n";
        }
        echo "\t</ul><br />\n";
    }

    // Trial attributes
    if($attributes){
        echo "\t<li>Attributes:<br />\n";
        echo "\t<ul style=\"list-style:none\">\n";
        foreach($attributes as $attr){
            echo "\t\t<li>" . $attr['attr_name'] . ", " . $attr['num_groups'] . " groups, Weight: " . $attr['weight'];
            if($attr['grouping_factor'] == "true")
                echo ", Grouping factor";
            echo "</li>\n";

            $groupQuery = $db->prepare("SELECT * FROM GROUPS WHERE attribute_id=:id");
            $groupQuery->setFetchMode(PDO::FETCH_ASSOC);
            $groupQuery->execute(array(":id" => $attr['id']));
            $groups = $groupQuery->fetchAll();

            if($groups){
                echo "\t\t<ul>\n";
                foreach($groups as $grp)
                    echo "\t\t\t<li>" . $grp['name'] . " (" . $grp['range_min'] . " to " . $grp['range_max'] . ")</li>\n";
                echo "\t\t</ul>\n";
            }
        }
        echo "\t</ul>\n";
    }
    echo "</ul>\n";
    echo "<h2>Trial statistics</h2>\n";
    echo "\t<p>Total participants: " . $totalAllocations . "</p>\n";
?>
	<a href="index.php">Return to trials</a>
</body>
</html>
