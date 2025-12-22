package com.unilumin.smartapp.ui.screens.site

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unilumin.smartapp.client.data.SiteRoadInfo
import com.unilumin.smartapp.ui.theme.Blue600
import com.unilumin.smartapp.ui.theme.Gray400
import com.unilumin.smartapp.ui.theme.Gray700
import com.unilumin.smartapp.ui.theme.Gray900
import kotlin.collections.forEach

@Composable
fun SearchAndFilterSection(
    selectedRoad: SiteRoadInfo?,
    siteRoadInfo: List<SiteRoadInfo>?,
    searchKeyword: String,
    isDropdownExpanded: Boolean,
    isMapView: Boolean,
    onDropdownToggle: (Boolean) -> Unit,
    onRoadSelected: (SiteRoadInfo?) -> Unit,
    onKeywordChanged: (String) -> Unit,
    onClearKeyword: () -> Unit,
    onToggleViewMode: () -> Unit,
    focusManager: FocusManager
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.size(44.dp)
                .clickable { onToggleViewMode() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isMapView) Icons.Rounded.FormatListBulleted else Icons.Rounded.Map,
                    contentDescription = "Switch View",
                    tint = Blue600,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Box(modifier = Modifier.weight(0.3f)) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clickable { onDropdownToggle(!isDropdownExpanded) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedRoad?.name ?: "区域",
                        fontSize = 13.sp,
                        color = if (selectedRoad != null) Blue600 else Gray700,
                        fontWeight = if (selectedRoad != null) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (isDropdownExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Gray400,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(12.dp))) {
                DropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { onDropdownToggle(false) },
                    modifier = Modifier.background(Color.White).width(140.dp)
                ) {
                    DropdownMenuItem(text = { Text("全部区域", fontSize = 14.sp) }, onClick = { onRoadSelected(null) })
                    siteRoadInfo?.forEach { road ->
                        DropdownMenuItem(text = { Text(road.name, fontSize = 14.sp) }, onClick = { onRoadSelected(road) })
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier
                .weight(0.6f)
                .height(44.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Search, null, tint = Gray400, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = searchKeyword,
                    onValueChange = onKeywordChanged,
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(fontSize = 13.sp, color = Gray900),
                    singleLine = true,
                    cursorBrush = SolidColor(Blue600),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    decorationBox = { innerTextField ->
                        if (searchKeyword.isEmpty()) Text("搜索站点...", color = Gray400, fontSize = 13.sp)
                        innerTextField()
                    }
                )
                if (searchKeyword.isNotEmpty()) {
                    IconButton(onClick = onClearKeyword, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Rounded.Close, null, tint = Gray400, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}