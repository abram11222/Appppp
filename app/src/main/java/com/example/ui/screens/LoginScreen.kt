package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.ui.components.ServiceBrandHeader
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeaconGrade
import com.example.model.LoginResult
import com.example.ui.theme.ModernPrimary
import com.example.ui.theme.ModernPrimaryDark
import com.example.ui.theme.ModernSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLogin: (emailOrPhone: String, pass: String) -> LoginResult,
    onRegisterServant: (
        name: String,
        phone: String,
        email: String,
        pass: String,
        requestedGrade: DeaconGrade,
        onComplete: (Boolean, String) -> Unit
    ) -> Unit,
    onOpenFirebaseSettings: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Register

    // Login Form State (Default empty for production)
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var pendingServantName by remember { mutableStateOf<String?>(null) }
    var isLoggingIn by remember { mutableStateOf(false) }

    // Register Form State
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regGrade by remember { mutableStateOf(DeaconGrade.PRIMARY_4) }
    var isGradeMenuExpanded by remember { mutableStateOf(false) }
    var regError by remember { mutableStateOf<String?>(null) }
    var isRegistering by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successDialogMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    val headerGradient = Brush.verticalGradient(
        colors = listOf(
            ModernPrimaryDark,
            ModernPrimary,
            Color(0xFFF8FAFC)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .imePadding()
    ) {
        // Decorative top modern banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .background(headerGradient)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Service Logo & Official Titles
            ServiceBrandHeader(
                logoSize = 104.dp,
                textColor = Color.White
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Main Pure White Container Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Modern Tab Selector: Login vs Register
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFFF1F5F9),
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = ModernPrimary,
                                height = 3.dp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                errorMessage = null
                                pendingServantName = null
                            },
                            text = {
                                Text(
                                    text = "تسجيل الدخول",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 0) ModernPrimary else TextSecondary,
                                    fontSize = 14.sp
                                )
                            },
                            icon = {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (selectedTab == 0) ModernPrimary else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )

                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                regError = null
                            },
                            text = {
                                Text(
                                    text = "إنشاء حساب خادم",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 1) ModernPrimary else TextSecondary,
                                    fontSize = 14.sp
                                )
                            },
                            icon = {
                                Icon(
                                    Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    tint = if (selectedTab == 1) ModernPrimary else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (selectedTab == 0) {
                        // ================= LOGIN TAB =================
                        Text(
                            text = "أهلاً بك في منصة خدمة الشمامسة",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "سجل دخولك لمتابعة الفصول والحضور ورصد التسميع والافتقاد",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )

                        // Pending Approval Alert Card
                        AnimatedVisibility(visible = pendingServantName != null) {
                            Surface(
                                color = Color(0xFFFFFBEB),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassTop,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "طلبك قيد المراجعة والموافقة ⏳",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF92400E)
                                            )
                                        )
                                        Text(
                                            text = "يا خادم المسيح (${pendingServantName ?: ""})، تم استلام طلب تسجيلك بنجاح وهو معروض الآن في لوحة تحكم أمين الخدمة لاعتماده وتحديد فصلك وصلاحياتك.",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                color = Color(0xFFB45309)
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Email / Username Field
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = {
                                emailInput = it
                                errorMessage = null
                                pendingServantName = null
                            },
                            label = { Text("اسم المستخدم أو البريد الإلكتروني") },
                            placeholder = { Text("اسم الخادم أو البريد") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = ModernPrimary
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Password Field
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                errorMessage = null
                                pendingServantName = null
                            },
                            label = { Text("كلمة المرور") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = ModernPrimary
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (isPasswordVisible) "إخفاء" else "إظهار",
                                        tint = TextSecondary
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (emailInput.isNotBlank() && passwordInput.isNotBlank() && !isLoggingIn) {
                                        isLoggingIn = true
                                        coroutineScope.launch {
                                            delay(300)
                                            val result = onLogin(emailInput, passwordInput)
                                            isLoggingIn = false
                                            when (result) {
                                                is LoginResult.Success -> {
                                                    // logged in
                                                }
                                                is LoginResult.PendingApproval -> {
                                                    pendingServantName = result.servantName
                                                    errorMessage = null
                                                }
                                                is LoginResult.Inactive -> {
                                                    errorMessage = result.message
                                                    pendingServantName = null
                                                }
                                                is LoginResult.Error -> {
                                                    errorMessage = result.message
                                                    pendingServantName = null
                                                }
                                            }
                                        }
                                    }
                                }
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input")
                        )

                        // Error Banner
                        AnimatedVisibility(visible = errorMessage != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                            ) {
                                Text(
                                    text = "⚠️ ${errorMessage ?: ""}",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    modifier = Modifier.padding(10.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                if (emailInput.isBlank() || passwordInput.isBlank()) {
                                    errorMessage = "يرجى كتابة البريد وكلمة المرور"
                                    return@Button
                                }
                                isLoggingIn = true
                                coroutineScope.launch {
                                    delay(300)
                                    val result = onLogin(emailInput, passwordInput)
                                    isLoggingIn = false
                                    when (result) {
                                        is LoginResult.Success -> {
                                            // Navigation handled by app observing currentServant
                                        }
                                        is LoginResult.PendingApproval -> {
                                            pendingServantName = result.servantName
                                            errorMessage = null
                                        }
                                        is LoginResult.Inactive -> {
                                            errorMessage = result.message
                                            pendingServantName = null
                                        }
                                        is LoginResult.Error -> {
                                            errorMessage = result.message
                                            pendingServantName = null
                                        }
                                    }
                                }
                            },
                            enabled = !isLoggingIn,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ModernPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("login_submit_button")
                        ) {
                            if (isLoggingIn) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "تسجيل الدخول",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    } else {
                        // ================= REGISTER TAB =================
                        Text(
                            text = "طلب انضمام خادم جديد",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "املأ بياناتك وسيتم إرسال طلبك فورياً للأدمن لاعتماده وتحديد فصلك وصلاحياتك",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )

                        // Name
                        OutlinedTextField(
                            value = regName,
                            onValueChange = {
                                regName = it
                                regError = null
                            },
                            label = { Text("اسم الخادم ثلاثي / رباعي *") },
                            placeholder = { Text("مثال: مينا عماد فهيم") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = ModernPrimary)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_name_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Email
                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = {
                                regEmail = it
                                regError = null
                            },
                            label = { Text("البريد الإلكتروني أو اسم الحساب *") },
                            placeholder = { Text("mina@church.org") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = ModernPrimary)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_email_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Requested Grade Dropdown
                        ExposedDropdownMenuBox(
                            expanded = isGradeMenuExpanded,
                            onExpandedChange = { isGradeMenuExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = regGrade.arabicTitle,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("الفصل المقترح للخدمة به *") },
                                leadingIcon = {
                                    Icon(Icons.Default.School, contentDescription = null, tint = ModernPrimary)
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGradeMenuExpanded)
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("register_grade_selector")
                            )

                            ExposedDropdownMenu(
                                expanded = isGradeMenuExpanded,
                                onDismissRequest = { isGradeMenuExpanded = false }
                            ) {
                                DeaconGrade.values().forEach { grade ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(grade.arabicTitle, fontWeight = FontWeight.Medium)
                                            }
                                        },
                                        onClick = {
                                            regGrade = grade
                                            isGradeMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password
                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = {
                                regPassword = it
                                regError = null
                            },
                            label = { Text("كلمة المرور *") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = ModernPrimary)
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_password_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Confirm Password
                        OutlinedTextField(
                            value = regConfirmPassword,
                            onValueChange = {
                                regConfirmPassword = it
                                regError = null
                            },
                            label = { Text("تأكيد كلمة المرور *") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = ModernPrimary)
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_confirm_password_input")
                        )

                        // Register Error Alert
                        AnimatedVisibility(visible = regError != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                            ) {
                                Text(
                                    text = "⚠️ ${regError ?: ""}",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    modifier = Modifier.padding(10.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Register Submit Button
                        Button(
                            onClick = {
                                if (regName.isBlank() || regEmail.isBlank() || regPassword.isBlank()) {
                                    regError = "يرجى ملء جميع الحقول المطلوبة"
                                    return@Button
                                }
                                if (regPassword != regConfirmPassword) {
                                    regError = "كلمة المرور وتأكيدها غير متطابقين"
                                    return@Button
                                }
                                if (regPassword.length < 3) {
                                    regError = "كلمة المرور يجب أن تتكون من 3 أحرف أو أرقام على الأقل"
                                    return@Button
                                }

                                isRegistering = true
                                onRegisterServant(regName, "", regEmail, regPassword, regGrade) { success, message ->
                                    isRegistering = false
                                    if (success) {
                                        successDialogMessage = message
                                        showSuccessDialog = true
                                        // Set to login field for convenience
                                        emailInput = regEmail
                                        passwordInput = regPassword
                                    } else {
                                        regError = message
                                    }
                                }
                            },
                            enabled = !isRegistering,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ModernSuccess,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("register_submit_button")
                        ) {
                            if (isRegistering) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "إرسال طلب الانضمام كخادم",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Firebase Live Sync Info Footer
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenFirebaseSettings() }
                    .padding(horizontal = 4.dp)
                    .testTag("firebase_info_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = ModernPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "سحابة Firebase Firestore المباشرة مفعلة",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "طلبات الخدام والتعديلات والحضور تنعكس للجميع فورياً",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }

    // Success Dialog on Register
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                selectedTab = 0
                pendingServantName = regName
            },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ModernSuccess,
                    modifier = Modifier.size(44.dp)
                )
            },
            title = {
                Text(
                    text = "تم إرسال طلبك بنجاح! 🎉",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "$successDialogMessage\n\nتم إرسال إشعار وطلب في شاشة الأدمن للموافقة وتحديد فصلك وصلاحياتك.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        selectedTab = 0
                        pendingServantName = regName
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ModernPrimary)
                ) {
                    Text("الانتقال إلى شاشة الدخول")
                }
            }
        )
    }
}
