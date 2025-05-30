/**
 * Misc utils.
 */

package phonon.nodes.utils


// check restrictions on string inputs
public fun stringInputIsValid(s: String, maxLength: Int = 32): Boolean {
    if ( s.length > maxLength ) {
        return false
    }

    if ( s.contains("\"") || s.contains("{") || s.contains("}") ) {
        return false
    }
    
    return true
}

// escape format string characters
public fun sanitizeString(s: String): String {
    var sEscaped = s.replace("$", "\$")
    sEscaped = sEscaped.replace("%", "%%")
    sEscaped = sEscaped.replace("\n", "")
    sEscaped = sEscaped.replace("\"", "")
    sEscaped = sEscaped.replace("{", "")
    sEscaped = sEscaped.replace("}", "")

    return sEscaped
}


/**
 * Estimate number of digits in a number, divide and conquer apparently fastest:
 * https://www.baeldung.com/java-number-of-digits-in-int
 * 
 * copypastad, only works up to 10 digits and on POSITIVE inputs
 * this will by default take absolute value
 */
public fun estimateNumDigits(x: Int): Int {
    val number = Math.abs(x)
    if ( number < 100000 ) {
        if ( number < 100 ) {
            if ( number < 10 ) {
                return 1
            } else {
                return 2
            }
        } else {
            if ( number < 1000 ) {
                return 3
            } else {
                if (number < 10000) {
                    return 4
                } else {
                    return 5
                }
            }
        }
    } else {
        if (number < 10000000) {
            if (number < 1000000) {
                return 6
            } else {
                return 7
            }
        } else {
            if (number < 100000000) {
                return 8
            } else {
                if (number < 1000000000) {
                    return 9
                } else {
                    return 10
                }
            }
        }
    }

    // return next closest num digits
    return 11
}