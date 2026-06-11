package com.mr.claudetraining.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.tasty_title),
                fontWeight = FontWeight.Black,
                fontSize = 24.sp
            )
        },
        actions = {
            IconButton(onClick = { }) {
                IconPlaceholder(Modifier.size(32.dp))
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White
        )
    )
}

@Composable
fun IconPlaceholder(modifier: Modifier = Modifier, color: Color = Color.Gray) {
    Box(
        modifier = modifier
            .size(24.dp)
            .background(color, RoundedCornerShape(4.dp))
    )
}

@Composable
fun HeroSection(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(350.dp)) {
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.5f),
                style = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.3f),
                radius = size.minDimension / 1.5f,
                style = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0xFFE31E24), fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.hero_title_part1))
                    }
                    append("\n")
                    withStyle(style = SpanStyle(color = Color.Black, fontWeight = FontWeight.Normal)) {
                        append(stringResource(R.string.hero_title_part2))
                    }
                },
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                lineHeight = 38.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = ColorPainter(Color.Gray),
                    contentDescription = null,
                    modifier = Modifier
                        .size(240.dp)
                        .clip(CircleShape)
                        .border(4.dp, Color.White, CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                OrbitalImage(Modifier.offset(x = (-100).dp, y = 80.dp), size = 80.dp)
                OrbitalImage(Modifier.offset(x = 100.dp, y = 80.dp), size = 80.dp)
                OrbitalImage(Modifier.offset(x = 0.dp, y = 110.dp), size = 80.dp)
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6600)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(56.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.see_more), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconPlaceholder(modifier = Modifier.size(16.dp), color = Color.White)
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconPlaceholder()
            IconPlaceholder()
            IconPlaceholder()
        }
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
            .border(2.dp, Color.White, CircleShape),
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
            .width(160.dp)
            .height(240.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box {
            Image(
                painter = ColorPainter(Color.Gray),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(text = name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(
                    text = stringResource(R.string.from_price, price),
                    color = Color(0xFFFF6600),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6600)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(text = stringResource(R.string.order_now), fontSize = 10.sp)
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
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.our_suggest_desc),
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
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
        modifier = modifier.width(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                Image(
                    painter = ColorPainter(Color.Gray),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    color = Color.Red,
                    shape = RoundedCornerShape(topStart = 16.dp, bottomEnd = 16.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = discount,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconPlaceholder(Modifier.size(16.dp), color = Color(0xFFFFCC00))
                    Text(text = " $rating", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(text = " | +100 review", color = Color.Gray, fontSize = 10.sp)
                }
                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "Made with juicy beef burger, cheese...", color = Color.Gray, fontSize = 10.sp, maxLines = 1)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = price, color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            Image(
                painter = ColorPainter(Color.Gray),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f)
                    .align(Alignment.CenterStart),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpeechBubble(
                    text = "Lorem ipsum dolor sit amet consectetur. Dolor vel euismod mus purus bibendum. Donec imperdiet non netus velit.",
                    color = Color(0xFFFFCC00),
                    showAdminIcon = true
                )
                SpeechBubble(
                    text = "Lorem ipsum dolor sit amet consec tetur. Dolor vel euismod.",
                    color = Color(0xFF6B8E23),
                    showUserIcon = true
                )
                SpeechBubble(
                    text = "Lorem ipsum dolor sit amet consec tetur. Dolor vel euismod mus purus bibendum. Donec.",
                    color = Color(0xFF556B2F),
                    showUserIcon = true
                )
            }
        }
    }
}

@Composable
fun SpeechBubble(text: String, color: Color, showAdminIcon: Boolean = false, showUserIcon: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (showUserIcon) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
                Text("user name", fontSize = 8.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Surface(
            color = color,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(12.dp),
                fontSize = 10.sp,
                color = Color.White
            )
        }
        if (showAdminIcon) {
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .border(1.dp, Color.Black, CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconPlaceholder(Modifier.size(16.dp))
                }
                Text("Admin", fontSize = 8.sp)
            }
        }
    }
}

@Composable
fun FAQSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.faq),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        repeat(3) {
            FAQItem()
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun FAQItem() {
    Surface(
        color = Color(0xFFF5F5F5),
        shape = RoundedCornerShape(12.dp),
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
                modifier = Modifier.weight(1f)
            )
            IconPlaceholder(Modifier.size(16.dp), color = Color.LightGray)
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
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.about_us_desc),
            textAlign = TextAlign.Center,
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

@Composable
fun FooterSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFFF6600))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = buildAnnotatedString {
                append("get the ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                    append("Best & Tasty")
                }
                append(" Foods\nwith Hight Quality")
            },
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(48.dp)
        ) {
            Text(text = stringResource(R.string.order_now), color = Color(0xFFFF6600), fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            AppStoreButtonPlaceholder("Google Play")
            AppStoreButtonPlaceholder("App Store")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            IconPlaceholder(color = Color.White)
            IconPlaceholder(color = Color.White)
            IconPlaceholder(color = Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.copyright),
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 10.sp
        )
    }
}

@Composable
fun AppStoreButtonPlaceholder(text: String) {
    Box(
        modifier = Modifier
            .width(120.dp)
            .height(40.dp)
            .background(Color.Black, RoundedCornerShape(8.dp))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontSize = 10.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun TastyDashboardPreview() {
    SrteamChatTheme {
        TastyDashboardScreen()
    }
}
