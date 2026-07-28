// Logo section
                        DesignCard("Logo de la constancia") {
                            Text("Sube un logo que aparecerá en la esquina superior izquierda del DC-3", fontSize = 13.sp, color = Gray500)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .background(Gray50, shape = RoundedCornerShape(12.dp))
                                    .border(2.dp, Gray200, RoundedCornerShape(12.dp))
                                    .clickable { logoLauncher.launch("image/*") }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (logoBase64 != null) {
                                    AsyncImage(model = logoBase64, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                                } else {
                                    Icon(Icons.Default.Upload, contentDescription = null, tint = Gray400, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Toca para subir logo (PNG/JPG)", fontSize = 13.sp, color = Gray400)
                                }
                            }
                        }