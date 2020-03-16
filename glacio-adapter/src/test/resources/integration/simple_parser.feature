# language: en

Feature: Simple parser feature
    Could execute simple tasks, i.e. ones without target, strategy, inputs nor outputs

    Scenario: Simple Success/Debug
        Given Do debug
        And Execute (success) direct success
        When something is good
            Do (debug) substep debug
        And it is very good
            Execute success
        Then it is very good
            it is very good
                Do debug
        And it is very good
            it is very good
                Execute (success) subsubstep success

    Scenario: Direct Fail
        When Do fail

    Scenario: SubStep fail
        When something is good
            Do (fail) substep fail

    Scenario: SubSubStep fail
        When it is very good
            it is very good
                Execute fail