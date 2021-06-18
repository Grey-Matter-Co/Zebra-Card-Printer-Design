<?xml version="1.0" encoding="utf-8"?>
<CardDesignProject Version="12" Name="CardTemplate2" Description="" CardWidth="327.647243760288" CardHeight="204.774802940736" HasIDChip="False" HasMagstripe="False" HasLaminate="False" HasOverlay="False" HasUV="False" IsSingleSided="True" WriteDirection="LeftToRight" xmlns="http://schemas.datacontract.org/2004/07/aCardDesigner">
    <DynamicFields>
        <DynamicField Name="Firstname" TypeName="default" TextFormat="{0}" DataFieldName="" DefaultValue="" IsFixedLength="False" FixedLength="10" Alignment="Left" PaddingValue="." />
        <DynamicField Name="Lastname" TypeName="default" TextFormat="{0}" DataFieldName="" DefaultValue="" IsFixedLength="False" FixedLength="10" Alignment="Left" PaddingValue="." />
        <DynamicField Name="Student ID" TypeName="default" TextFormat="{0}" DataFieldName="" DefaultValue="" IsFixedLength="False" FixedLength="10" Alignment="Left" PaddingValue="." />
    </DynamicFields>
    <FrontRibbon Name="" RibbonType="Color" Orientation="Landscape">
        <Layers>
            <Layer Name="Design" LayerType="Design" />
        </Layers>
    </FrontRibbon>
    <BackRibbon Name="" RibbonType="Color" Orientation="Landscape">
        <Layers>
            <Layer Name="Design" LayerType="Design" />
        </Layers>
    </BackRibbon>
    <FrontDocument Name="">
        <Layers>
            <XamlDesignLayer Name="Design">
                <Elements>
                    <XamlImageElement Source="d465259e-066d-4014-b94a-ec81a14eaa83.png" Transparency="0" PixelInterpolationMode="Default" Name="" Top="0" Left="0" Width="327.647243760288" Height="204.774802940736" FixedHeightWidthRatio="False" WidthRatio="1" HeightRatio="1" ZIndex="0" Angle="0" Visible="True" Locked="False" MarginLeft="0" MarginTop="0" MarginRight="0" MarginBottom="0" ForegroundColor="#00000000" BackgroundColor="#00ffffff">
                        <Border Color="#ff000000" Thickness="0" CornerRadius="0" Style="Solid" />
                    </XamlImageElement>
                    <XamlTextElement Text="{Firstname}&#xD;&#xA;{Lastname}" TextColor="#ffffffff" AlignmentV="Top" AlignmentH="Center" FontFamily="Franklin Gothic Medium" FontStyle="Normal" FontWeight="Bold" FontSize="10.6666666666667" TextDecorations="" WrapText="False" ClipContent="True" FitContentToSize="False" Name="" Top="148.130887243436" Left="202.541954363671" Width="99.1483115126113" Height="29.6958842287245" FixedHeightWidthRatio="False" WidthRatio="1" HeightRatio="1" ZIndex="2" Angle="0" Visible="True" Locked="False" MarginLeft="0" MarginTop="0" MarginRight="0" MarginBottom="0" ForegroundColor="#00000000" BackgroundColor="#00ffffff">
                        <Border Color="#ff000000" Thickness="0" CornerRadius="0" Style="Solid" />
                    </XamlTextElement>
                    <XamlTextElement Text="{Student ID}" TextColor="#ffffffff" AlignmentV="Top" AlignmentH="Center" FontFamily="Franklin Gothic Medium" FontStyle="Normal" FontWeight="Bold" FontSize="9" TextDecorations="" WrapText="False" ClipContent="True" FitContentToSize="False" Name="" Top="174.153615598578" Left="202.60487889988" Width="98.7622864905758" Height="13.1751606023911" FixedHeightWidthRatio="False" WidthRatio="1" HeightRatio="1" ZIndex="3" Angle="0" Visible="True" Locked="False" MarginLeft="0" MarginTop="0" MarginRight="0" MarginBottom="0" ForegroundColor="#00000000" BackgroundColor="#00ffffff">
                        <Border Color="#ff000000" Thickness="0" CornerRadius="0" Style="Solid" />
                    </XamlTextElement>
                    <XamlTextElement Text="GREEN VALLEY " TextColor="#ffffffff" AlignmentV="Top" AlignmentH="Center" FontFamily="Copperplate Gothic Bold" FontStyle="Normal" FontWeight="Normal" FontSize="18.6666666666667" TextDecorations="" WrapText="False" ClipContent="True" FitContentToSize="False" Name="" Top="147.628346306112" Left="0.289594804952415" Width="200.281556129293" Height="21.429921237984" FixedHeightWidthRatio="False" WidthRatio="1" HeightRatio="1" ZIndex="4" Angle="0" Visible="True" Locked="False" MarginLeft="0" MarginTop="0" MarginRight="0" MarginBottom="0" ForegroundColor="#00000000" BackgroundColor="#00ffffff">
                        <Border Color="#ff000000" Thickness="0" CornerRadius="0" Style="Solid" />
                    </XamlTextElement>
                    <XamlTextElement Text="UNIVERSITY" TextColor="#ffffffff" AlignmentV="Top" AlignmentH="Center" FontFamily="Copperplate Gothic Bold" FontStyle="Normal" FontWeight="Normal" FontSize="13.3333333333333" TextDecorations="" WrapText="False" ClipContent="True" FitContentToSize="False" Name="" Top="170.532283290624" Left="0.733095262473867" Width="198.628606854107" Height="18.179527540512" FixedHeightWidthRatio="False" WidthRatio="1" HeightRatio="1" ZIndex="5" Angle="0" Visible="True" Locked="False" MarginLeft="0" MarginTop="0" MarginRight="0" MarginBottom="0" ForegroundColor="#00000000" BackgroundColor="#00ffffff">
                        <Border Color="#ff000000" Thickness="0" CornerRadius="0" Style="Solid" />
                    </XamlTextElement>
                    <XamlPassportPhotoElement Transparency="0" PixelInterpolationMode="Default" StretchMode="Fill" Name="Passport photo" Top="-1.16342415016786" Left="201.602829405086" Width="100.400903444382" Height="133.867871259176" FixedHeightWidthRatio="True" WidthRatio="3" HeightRatio="4" ZIndex="6" Angle="0" Visible="True" Locked="False" MarginLeft="0" MarginTop="0" MarginRight="0" MarginBottom="0" ForegroundColor="#00000000" BackgroundColor="#00ffffff">
                        <Border Color="#ffffffff" Thickness="1.13385826771654" CornerRadius="0" Style="Solid" />
                    </XamlPassportPhotoElement>
                </Elements>
            </XamlDesignLayer>
        </Layers>
    </FrontDocument>
    <BackDocument Name="">
        <Layers>
            <XamlDesignLayer Name="Design">
                <Elements />
            </XamlDesignLayer>
        </Layers>
    </BackDocument>
</CardDesignProject>