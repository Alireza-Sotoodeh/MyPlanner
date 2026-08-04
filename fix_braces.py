import re

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'r') as f:
    content = f.read()

old_code = """                    }
                }
            } // Close AnimatedVisibility
        } // Close if (subtasks.isNotEmpty())
    } // Close Column
    // Quick Pomodoro Trigger"""

new_code = """                    }
                }
            } // Close AnimatedVisibility
        } // Close if (subtasks.isNotEmpty())
                } // Close Column inside AnimatedVisibility
            } // Close AnimatedVisibility block
        } // Close Column
        // Quick Pomodoro Trigger"""

content = content.replace(old_code, new_code)

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'w') as f:
    f.write(content)
