<?php

/**
 * PHP Version of the class java.util.Properties.  Loads and saves Java 
 * .properties files.
 *
 * The methods inherited from Hashtable are not implemented, as they are
 * merely another interface to the same functionality.  The save method is not 
 * implemented, as it is considered deprecated; use store instead.
 * 
 * In general, methods which are overloaded to accept an optional parameter
 * are combined into a single method with a default value of null for the
 * optional parameters.  Methods which take versions of streams, writers,
 * etc. are consolidated into a single method taking a PHP stream.
 *
 * The list() methods are replaced with debugList(), as list is a reserved
 * word in PHP.
 *
 * For more information on the .properties format, see
 *   http://java.sun.com/javase/6/docs/api/java/util/Properties.html
 */
class Properties
{
	/**
	 * Constructor.  Takes an optional Properties object defining the default
	 * values of the properties.
	 */
	public function __construct($defaults = null) {
		if($defaults != null) {
			$this->defaults = $defaults;
		}

		$this->properties = array();
	}

	/**
	 * Retrieves a property value.  If a value is not found for this key, the
	 * defaults object is searched for a default.  If not default value is 
	 * found there, the default value passed into this function is used.  If
	 * a value still cannot be determined, null is returned.
	 */
	public function getProperty($key, $defaultValue = null) {
		$value = null;

		if(array_key_exists($key, $this->properties)) {
			$value = $this->properties[$key];
		}

		if($value == null && $this->defaults != null) {
			$value = $this->defaults->getProperty($key);
		}

		if($value == null) {
			$value = $defaultValue;
		}

		return $value;
	}

	/**
	 * Sets a property value.  Returns the old value, if available.  No defaults
	 * are used when searching for the old value; a value is returned only if it
	 * was explicitly set.
	 */
	public function setProperty($key, $value) {
		$oldValue = null;
		if(array_key_exists($key, $this->properties)) {
			$oldValue = $this->properties[$key];
		}
		$this->properties[$key] = $value;
		return $oldValue;
	}

	/**
	 * Returns the names of all keys in this property set, including keys in the
	 * default property set if no value is defined in this property set.
	 */
	public function propertyNames() {
		$keys = array();
		if($this->defaults != null) {
			$keys = $this->defaults->propertyNames();
		}

		$myKeys = array_keys($this->properties);
		sort($myKeys);
		for($i = 0; $i < count($myKeys); $i++) {
			if(!array_key_exists($myKeys[$i], $keys)) {
				array_push($keys, $myKeys[$i]);
			}
		}

		return $keys;
	}

	/**
 	 * Writes the properties to the given stream, in the format defined by
	 * java.util.Properties.
	 */
	public function store($handle, $comment = null) {
		if($comment != null) {
			fwrite($handle, "#" . $this->escapeComment($comment) . PHP_EOL);
		}
		fwrite($handle, "#" . date("D M d H:i:s T Y") . PHP_EOL);

		$propertyNames = array_keys($this->properties);
		for($i = 0; $i < count($propertyNames); $i++) {
			fwrite($handle, $this->escapeKey($propertyNames[$i]) . "=" . $this->escapeValue($this->properties[$propertyNames[$i]]) . PHP_EOL);
		}
	}

	/**
	 * Loads a file from the given stream, in the format defined by
	 * java.util.Properties
	 */
	public function load($handle) {
		$this->properties = array();

		$logicalLine = "";
		$appendNext = false;
		while(!feof($handle)) {
			$line = fgets($handle);
			$line = rtrim($line, "\r\n"); // Chomp!
			$line = ltrim($line);

			// If the previous line was a continuation line, append this one
			if($appendNext) {
				$logicalLine .= $line;
				$appendNext = false;
			} else {
				$logicalLine = $line;
			}

			// Is it a comment line?
			// Check this before checking for continuations, as comments cannot be continued
			if(preg_match("/^[ 	]*[#!]/", $logicalLine)) {
				continue;
			}

			// Is this line continued?
			// A continuation line is one ending in an odd number of backslashes
			$numTrailingBackslashes = 0;
			while((strlen($logicalLine) - $numTrailingBackslashes - 1) > -1 && 
				  $logicalLine[strlen($logicalLine) - $numTrailingBackslashes - 1] == "\\") {
				$numTrailingBackslashes++;
			}
			if($numTrailingBackslashes % 2 == 1) {
				// If it is a continuation line, append a newline.. this will
				// removed when the line is unescaped, but appending it in 
				// advance prevents backslashes other than the final one from
				// escaping the next line, e.g. in the case
				//   a=b\\\
				//   n
				// If you are not careful, this will be transformed into
				//   a=b\\n
				// And bad things could happen.
				$logicalLine .= "\n";
				$appendNext = true;
				continue;
			}

			// Now that we've combined the physical lines to form one large 
			// logical line, see if it's empty.
			if(strcmp($logicalLine, "") == 0) {
				continue;
			}

			// Split into key-value pairs
			$matches = array();
			$key = "";
			$value = "";

			// This regex splits the string into 3 sections:
			//   - Leading whitespace plus all non-whitespace characters up to 
			//     the first unescaped :, =, space, or tab character
			//   - The :, =, space, or tab character itself, which separates 
			//     the key from the value
			//   - The rest of the string
			// If this match succeeds, the first and third pieces form the key 
			// and value. Otherwise, the entire string is considered to be a 
			// key with no value.
			if(preg_match("/^([ 	]*.*?[^\\\\])([:= 	]+?)([^:= 	].*)$/s", $logicalLine, $matches)) {
				$key = $matches[1];
				$value = $matches[3];
			} else {
				$key = $logicalLine;
			}

			$key = $this->unescapeGeneric($key);
			$value = $this->unescapeGeneric($value);

			$this->properties[$key] = $value;
		}
		
	}

	/**
	 * List the contents to an output stream.  This is useful for debugging.
	 */
	public function debugList($handle) {
		fwrite($handle, "-- listing properties --" . PHP_EOL);
		$propertyNames = $this->propertyNames();
		for($i = 0; $i < count($propertyNames); $i++) {
			fwrite($handle, $propertyNames[$i] . "=" . $this->getProperty($propertyNames[$i]) . PHP_EOL);
		}
	}

	private function escapeKey($key) {
		return str_replace(" ", "\\ ", $this->escapeGeneric($key));
	}

	private function escapeValue($value) {
		$value = $this->escapeGeneric($value);

		// Values have leading space replaced, but none other
		if(strlen($value) > 0 && strcmp($value[0], ' ') == 0) {
			$value = "\\" . $value;
		}

		return $value;
	}

	private function escapeGeneric($value) {
		$value = str_replace("#", "\\#", $value);
		$value = str_replace("!", "\\!", $value);
		$value = str_replace("=", "\\=", $value);
		$value = str_replace(":", "\\:", $value);
		$value = str_replace("\t", "\\t", $value);
		$value = str_replace("\n", "\\n", $value);
		$value = str_replace("\r", "\\r", $value);
		return $value;
	}

	private function escapeComment($comment) {
		return preg_replace("/\n([^#])/", "\n#\\1", $comment);
	}

	private function unescapeGeneric($value) {
		$value = preg_replace("/\\\\([^\\\\trn\\n])/s", "\\1", $value);
		$value = str_replace("\\\n", "", $value);
		$value = str_replace("\\t", "\t", $value);
		$value = str_replace("\\r", "\r", $value);
		$value = str_replace("\\n", "\n", $value);
		return $value;
	}

	private $properties /* Array */;
	private $defaults /* Properties */;
}

?>
