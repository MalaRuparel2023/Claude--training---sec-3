package com.mr.claudetraining.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ro.alexmamo.firebasesigninwithemailandpassword.R
import com.mr.claudetraining.ui.theme.SrteamChatTheme

// Tasty Brand Colors
val TastyRed = Color(0xFFE31E24)
val TastyOrange = Color(0xFFFF6600)
val TastyYellow = Color(0xFFFFCC00)
val TastyGreen = Color(0xFF6B8E23)
val TastyDarkGreen = Color(0xFF556B2F)

@Composable
fun TastyDashboardScreen(
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TastyTopBar() }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { HeroSection() }
            item { MenuCategoriesSection() }
            item { OurSuggestSection() }
            item { WhyUsSection() }
            item { FAQSection() }
            item { AboutUsSection() }
            item { FooterSection() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TastyTopBar(modifier: Modifier = Modifier) {
    TopAppBar(
        modifier = modifier.padding(horizontal = 8.dp),
        title = {
            Text(
                text = stringResource(R.string.tasty_title),
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                letterSpacing = 2.sp
            )
        },
        actions = {
            IconButton(onClick = { }) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}

@Composable
fun HeroSection(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Vertical Social Icons
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SocialIconPlaceholder()
            SocialIconPlaceholder()
            SocialIconPlaceholder()
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Title
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = TastyRed, fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.hero_title_part1))
                    }
                    append("\n")
                    withStyle(style = SpanStyle(color = Color.Black, fontWeight = FontWeight.Normal)) {
                        append(stringResource(R.string.hero_title_part2))
                    }
                },
                fontSize = 36.sp,
                textAlign = TextAlign.Center,
                lineHeight = 42.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Central Dish with Orbital Images
            Box(
                modifier = Modifier.size(360.dp),
                contentAlignment = Alignment.Center
            ) {
                // Dashed Circles
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius1 = size.minDimension / 2.2f
                    val radius2 = size.minDimension / 1.6f
                    
                    drawCircle(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        radius = radius1,
                        style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                    )
                    drawCircle(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        radius = radius2,
                        style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                    )
                }

                // Large Main Dish
                Image(
                    painter = ColorPainter(Color.Gray), // Replace with real image
                    contentDescription = null,
                    modifier = Modifier
                        .size(240.dp)
                        .clip(CircleShape)
                        .border(6.dp, Color(0xFFF0F0F0), CircleShape)
                        .padding(8.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                // Orbital Images
                OrbitalImage(
                    modifier = Modifier.align(Alignment.BottomStart).offset(x = 60.dp, y = (-80).dp),
                    size = 70.dp
                )
                OrbitalImage(
                    modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-20).dp),
                    size = 80.dp
                )
                OrbitalImage(
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-60).dp, y = (-80).dp),
                    size = 75.dp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // See More Button
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = TastyOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.see_more),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SocialIconPlaceholder() {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(Color.White, CircleShape)
            .border(1.dp, Color.LightGray, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Placeholder for social icons (IG, FB, TW)
        Box(modifier = Modifier.size(12.dp).background(Color.Gray, RoundedCornerShape(2.dp)))
    }
}

@Composable
fun OrbitalImage(modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp) {
    Image(
        painter = ColorPainter(Color.DarkGray),
        contentDescription = null,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(4.dp, Color.White, CircleShape)
            .padding(2.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun MenuCategoriesSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.menu_categories),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { CategoryCard(stringResource(R.string.salads), "8$") }
            item { CategoryCard(stringResource(R.string.pizza), "18$") }
            item { CategoryCard(stringResource(R.string.burger), "12$") }
        }
    }
}

@Composable
fun CategoryCard(name: String, price: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .width(180.dp)
            .height(260.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            Image(
                painter = ColorPainter(Color.Gray),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Dark Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                            startY = 300f
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(text = name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "from ", color = Color.White, fontSize = 14.sp)
                    Text(text = price, color = TastyOrange, fontWeight = FontWeight.Black, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = TastyOrange),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(text = stringResource(R.string.order_now), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OurSuggestSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.our_suggest),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.our_suggest_desc),
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { SuggestCard("Salad", "5.9", "8.5 $", "30%") }
            item { SuggestCard("Burger", "7.5", "9.5 $", "10%") }
            item { SuggestCard("Potato", "5.7", "7.20 $", "30%") }
        }
    }
}

@Composable
fun SuggestCard(name: String, rating: String, price: String, discount: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.width(220.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                Image(
                    painter = ColorPainter(Color.LightGray),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentScale = ContentScale.Crop
                )
                // Discount Badge
                Surface(
                    color = TastyRed,
                    shape = RoundedCornerShape(topStart = 24.dp, bottomEnd = 24.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = discount,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = TastyYellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(text = " $rating", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = " | +100 review", color = Color.Gray, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(
                    text = "Made with juicy beef burger, cheese...",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                         Text(
                            text = "9.20 $", // Placeholder old price
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                        Text(text = price, color = TastyRed, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun WhyUsSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.why_us),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        ) {
            // Splash Burger Image
            Image(
                painter = ColorPainter(Color.Gray),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.6f)
                    .align(Alignment.CenterStart)
                    .offset(x = (-40).dp),
                contentScale = ContentScale.FillHeight
            )
            
            // Speech Bubbles
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SpeechBubble(
                    text = "Lorem ipsum dolor sit amet consectetur. Dolor vel euismod mus purus bibendum. Donec imperdiet non netus velit.",
                    color = TastyYellow,
                    isAdmin = true
                )
                SpeechBubble(
                    text = "Lorem ipsum dolor sit amet consec tetur. Dolor vel euismod.",
                    color = TastyGreen,
                    isUser = true,
                    rating = 4
                )
                SpeechBubble(
                    text = "Lorem ipsum dolor sit amet consec tetur. Dolor vel euismod mus purus bibendum. Donec.",
                    color = TastyDarkGreen,
                    isUser = true,
                    rating = 3
                )
            }
        }
    }
}

@Composable
fun SpeechBubble(
    text: String,
    color: Color,
    isAdmin: Boolean = false,
    isUser: Boolean = false,
    rating: Int = 0
) {
    Row(verticalAlignment = Alignment.Bottom) {
        if (isUser) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
                Text("user name", fontSize = 10.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Surface(
            color = color,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = text,
                    fontSize = 12.sp,
                    color = Color.White,
                    lineHeight = 16.sp
                )
                if (rating > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (index < rating) TastyYellow else Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(text = " (15)", color = Color.White, fontSize = 10.sp)
                    }
                }
            }
        }

        if (isAdmin) {
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(1.dp, Color.Black, CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star, // Admin icon placeholder
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text("Admin", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FAQSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.faq),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        repeat(3) {
            FAQItem()
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun FAQItem() {
    Surface(
        color = Color(0xFFF5F5F5),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.faq_placeholder),
                fontSize = 12.sp,
                color = Color.DarkGray,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}

@Composable
fun AboutUsSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.about_us),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.about_us_desc),
            textAlign = TextAlign.Center,
            color = Color.Gray,
            fontSize = 16.sp,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun FooterSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TastyOrange)
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = buildAnnotatedString {
                append("get the ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Black, color = Color.White)) {
                    append("Best & Tasty")
                }
                append(" Foods\nwith Hight Quality")
            },
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 22.sp,
            lineHeight = 28.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
        ) {
            Text(
                text = stringResource(R.string.order_now),
                color = TastyOrange,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            StoreBadge("Google Play")
            StoreBadge("App Store")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            // Footer Social Icons
            Box(modifier = Modifier.size(24.dp).background(Color.White.copy(alpha = 0.2f), CircleShape))
            Box(modifier = Modifier.size(24.dp).background(Color.White.copy(alpha = 0.2f), CircleShape))
            Box(modifier = Modifier.size(24.dp).background(Color.White.copy(alpha = 0.2f), CircleShape))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.copyright),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
    }
}

@Composable
fun StoreBadge(label: String) {
    Surface(
        color = Color.Black,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.width(140.dp).height(45.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
             Box(modifier = Modifier.size(24.dp).background(Color.White, RoundedCornerShape(4.dp))) // Placeholder icon
             Spacer(modifier = Modifier.width(8.dp))
             Column {
                 Text(text = "GET IT ON", color = Color.White, fontSize = 8.sp)
                 Text(text = label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
             }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TastyDashboardPreview() {
    SrteamChatTheme {
        TastyDashboardScreen()
    }
}
