# Security Audit Skill

## Description
Perform a comprehensive security audit of ToneForge code, focusing on Android security best practices and the recent security improvements.

## When to Use
- Before releasing a new version
- After adding new file I/O operations
- When implementing new URI handling
- After modifying JNI/native code
- When adding new permissions

## Instructions

1. **Review Recent Security Fixes**
   - Verify buffer size validation in JNI functions
   - Check WAV file validation in LoopLoadUtil
   - Review URI permission handling in LoopShareUtil
   - Confirm FileProvider path restrictions

2. **JNI Security Audit**
   - Check for buffer overflows in native code
   - Verify array bounds checking
   - Look for potential memory leaks
   - Review string handling and null checks
   - Validate all input from Java/Kotlin layer

3. **File System Security**
   - Review all file I/O operations
   - Check FileProvider configuration (res/xml/file_paths.xml)
   - Verify path traversal protections
   - Ensure proper file permissions
   - Check external storage access

4. **Input Validation**
   - Validate all user inputs
   - Check audio file format validation
   - Review preset import/export validation
   - Verify MIDI input sanitization
   - Check URL/URI validation

5. **Permission Handling**
   - Review runtime permission requests
   - Check permission grant callbacks
   - Verify proper permission denials
   - Ensure no permission bypasses

6. **Data Protection**
   - Check for sensitive data logging
   - Review data encryption at rest
   - Verify secure data transmission
   - Check for hardcoded secrets or keys

7. **Android Security Best Practices**
   - Verify exported components are secured
   - Check intent filter security
   - Review WebView security (if any)
   - Verify SSL/TLS certificate validation
   - Check for SQL injection vulnerabilities

8. **Native Code Security**
   - Review use of unsafe C/C++ functions
   - Check integer overflow/underflow
   - Verify proper cleanup in destructors
   - Review pointer arithmetic safety

9. **Third-Party Dependencies**
   - Check for known vulnerabilities in dependencies
   - Review dependency versions
   - Verify dependency integrity

10. **Generate Report**
    - List all findings with severity (Critical, High, Medium, Low)
    - Provide code references for each issue
    - Suggest remediation steps
    - Prioritize fixes

## OWASP Mobile Top 10 Checklist
- M1: Improper Platform Usage
- M2: Insecure Data Storage
- M3: Insecure Communication
- M4: Insecure Authentication
- M5: Insufficient Cryptography
- M6: Insecure Authorization
- M7: Client Code Quality
- M8: Code Tampering
- M9: Reverse Engineering
- M10: Extraneous Functionality

## Expected Deliverables
- Detailed security audit report
- Prioritized list of vulnerabilities
- Code snippets showing issues
- Specific remediation recommendations
- Risk assessment for each finding
