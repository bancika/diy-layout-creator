#!/usr/bin/env python3
"""
Script to parse DIYLC update_archive.xml and update.xml files to generate
SQL INSERT statements for the diylc_knowledge_base table with JSON content.
"""
import xml.etree.ElementTree as ET
import json
import os

# Paths to the XML files
ARCHIVE_XML_PATH = "../diylc/diylc-core/src/main/resources/update_archive.xml"
CURRENT_XML_PATH = "../diylc/diylc-core/src/main/resources/update.xml"
OUTPUT_SQL_FILE = "diylc_version_history.sql"

def parse_xml_file(file_path):
    """Parse the XML file and extract version information."""
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        
        # The root is <java.util.Arrays_-ArrayList>
        # Inside it there is an <a> element containing <org.diylc.appframework.update.Version> elements
        versions = []
        
        for version_elem in root.find('a').findall('org.diylc.appframework.update.Version'):
            version_info = {}
            
            # Extract version number
            version_number = version_elem.find('versionNumber')
            major = version_number.find('major').text
            minor = version_number.find('minor').text
            build = version_number.find('build').text
            version_info['version'] = f"{major}.{minor}.{build}"
            
            # Extract release date if available
            release_date_elem = version_elem.find('releaseDate')
            if release_date_elem is not None and release_date_elem.text:
                version_info['releaseDate'] = release_date_elem.text
            
            # Extract name if available (might be empty)
            name_elem = version_elem.find('name')
            if name_elem is not None and name_elem.text:
                version_info['name'] = name_elem.text
            
            # Extract changes
            changes = []
            changes_elem = version_elem.find('changes')
            if changes_elem is not None:
                for change_elem in changes_elem.findall('org.diylc.appframework.update.Change'):
                    change_type_elem = change_elem.find('changeType')
                    description_elem = change_elem.find('description')
                    
                    if change_type_elem is not None and description_elem is not None:
                        # Replace underscores with spaces in change type
                        change_type = change_type_elem.text.replace('_', ' ')
                        description = description_elem.text
                        
                        if description:  # Skip empty descriptions
                            changes.append({
                                'type': change_type,
                                'description': description
                            })
            
            # Extract URL if available
            url_elem = version_elem.find('url')
            if url_elem is not None and url_elem.text:
                version_info['url'] = url_elem.text.strip()
            
            version_info['changes'] = changes
            versions.append(version_info)
        
        return versions
    except Exception as e:
        print(f"Error parsing XML file {file_path}: {e}")
        return []

def generate_sql_insert(version_info):
    """Generate SQL INSERT statement for a single version with JSON content."""
    version = version_info['version']
    
    # Create a JSON object with version info and changes
    json_data = {
        'version': version,
        'changes': version_info['changes']
    }
    
    # Add optional fields if available (excluding URL)
    for field in ['releaseDate', 'name']:
        if field in version_info:
            json_data[field] = version_info[field]
    
    # Convert to JSON string and escape single quotes for SQL
    json_string = json.dumps(json_data, indent=2).replace("'", "''")
    
    sql_insert = f"""-- Version {version}
DELETE FROM diylc_knowledge_base WHERE section = 'DIYLC Version {version}' and category = 'History';
INSERT INTO diylc_knowledge_base (category, section, content)
VALUES ('History', 'DIYLC Version {version}', 
'{json_string}');
"""
    return sql_insert

def generate_sql_file(versions, output_file):
    """Generate SQL file with INSERT statements for all versions."""
    with open(output_file, 'w') as f:
        f.write("-- SQL script to insert DIYLC version history into diylc_knowledge_base table\n")
        f.write("-- Generated from update_archive.xml and update.xml data\n")
        f.write("-- Content is stored as JSON for easier parsing and flexibility\n\n")
        
        # Sort versions by version number
        versions.sort(key=lambda v: [int(x) for x in v['version'].split('.')])
        
        # Remove duplicates (prefer entries from update.xml over update_archive.xml)
        unique_versions = {}
        for version_info in versions:
            unique_versions[version_info['version']] = version_info
        
        for version_info in unique_versions.values():
            sql_insert = generate_sql_insert(version_info)
            f.write(sql_insert)
            f.write("\n")

if __name__ == "__main__":
    # Check if files exist
    versions = []
    
    # Try to parse the archive file
    if os.path.exists(ARCHIVE_XML_PATH):
        print(f"Parsing archive XML file: {ARCHIVE_XML_PATH}")
        archive_versions = parse_xml_file(ARCHIVE_XML_PATH)
        print(f"Found {len(archive_versions)} versions in archive file.")
        versions.extend(archive_versions)
    else:
        print(f"Warning: Archive XML file not found at {ARCHIVE_XML_PATH}")
    
    # Try to parse the current file
    if os.path.exists(CURRENT_XML_PATH):
        print(f"Parsing current XML file: {CURRENT_XML_PATH}")
        current_versions = parse_xml_file(CURRENT_XML_PATH)
        print(f"Found {len(current_versions)} versions in current file.")
        versions.extend(current_versions)
    else:
        print(f"Warning: Current XML file not found at {CURRENT_XML_PATH}")
    
    if not versions:
        print("Error: No versions found in either file.")
        exit(1)
    
    print(f"Total unique versions found: {len(set(v['version'] for v in versions))}")
    generate_sql_file(versions, OUTPUT_SQL_FILE)
    print(f"SQL file generated: {OUTPUT_SQL_FILE}")
    print("Done!") 