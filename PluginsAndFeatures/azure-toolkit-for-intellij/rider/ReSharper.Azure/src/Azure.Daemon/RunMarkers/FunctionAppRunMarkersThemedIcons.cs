//------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated by a tool.
//     Runtime Version:4.0.30319.42000
//
//     Changes to this file may cause incorrect behavior and will be lost if
//     the code is regenerated.
// </auto-generated>
//------------------------------------------------------------------------------

namespace JetBrains.UI.ThemedIcons
{
	/// <summary>
	///	<para>
	///		<para>Autogenerated identifier classes and identifier objects to Themed Icons registered with <see cref="JetBrains.Application.Icons.IThemedIconManager"></see>.</para>
	///		<para>Identifier classes should be used in attributes, XAML, or generic parameters. Where an <see cref="JetBrains.UI.Icons.IconId"></see> value is expected, use the identifier object in the <c>Id</c> field of the identifier class.</para>
	///	</para>
	///</summary>
	///<remarks>
	///	<para>This code was compile-time generated to support Themed Icons in the JetBrains application.</para>
	///	<para>It has two primary goals: load the icons of this assembly to be registered with <see cref="JetBrains.Application.Icons.IThemedIconManager"></see> so that they were WPF-accessible and theme-sensitive; and emit early-bound accessors for referencing icons in codebehind in a compile-time-validated manner.</para>
	///	<h1>XAML</h1>
	///	<para>For performance reasons, the icons are not individually exposed with application resources. There is a custom markup extension to bind an image source in markup.</para>
	///	<para>To use an icon from XAML, set an <see cref="System.Windows.Media.ImageSource"></see> property to the <see cref="System.CodeDom.CodeTypeReference"></see> markup extension, which takes an icon identifier class (nested in <see cref="FunctionAppRunMarkersThemedIcons"></see> class) as a parameter.</para>
	///	<para>Example:</para>
	///	<code>&lt;Image Source="{icons:ThemedIcon myres:DodofuxThemedIconsThemedIcons+Trinity}" /&gt;</code>
	///	<h1>Attributes</h1>
	///	<para>Sometimes you have to reference an icon from a type attriute when you're defining objects in code. Typical examples are Options pages and Tool Windows.</para>
	///	<para>To avoid the use of string IDs which are not validated very well, we've emitted identifier classes to be used with <c>typeof()</c> expression, one per each icon. Use the attribute overload which takes a <see cref="System.Type"></see> for an image, and choose your icon class from nested classes in the <see cref="FunctionAppRunMarkersThemedIcons"></see> class.</para>
	///	<para>Example:</para>
	///	<code>[Item(Name="Sample", Icon=typeof(DodofuxThemedIconsThemedIcons.Trinity))]</code>
	///	<h1>CodeBehind</h1>
	///	<para>In codebehind, we have two distinct tasks: (a) specify some icon in the APIs and (b) render icon images onscreen.</para>
	///	<para>On the APIs stage you should only manipulate icon identifier objects (of type <see cref="JetBrains.UI.Icons.IconId"></see>, statically defined in <see cref="FunctionAppRunMarkersThemedIcons"></see> in <c>Id</c> fields). Icon identifier classes (nested in <see cref="FunctionAppRunMarkersThemedIcons"></see>) should be turned into icon identifier objects as early as possible. Rendering is about getting an <see cref="System.Windows.Media.ImageSource"></see> to assign to WPF, or <see cref="System.Drawing.Bitmap"></see> to use with GDI+ / Windows Forms.</para>
	///	<para>You should turn an identifier object into a rendered image as late as possible. The identifier is static and lightweight and does not depend on the current theme, while the image is themed and has to be loaded or generated/rasterized. You need an <see cref="JetBrains.Application.Icons.IThemedIconManager"></see> instance to get the image out of an icon identifier object. Once you got the image, you should take care to change it with theme changes — either by using a live image property, or by listening to the theme change event. See <see cref="JetBrains.Application.Icons.IThemedIconManager"></see> and its extensions for the related facilities.</para>
	///	<para>Example:</para>
	///	<code>// Getting IconId identifier object to use with APIs
	///IconId iconid = DodofuxThemedIconsThemedIcons.Trinity.Id;</code>
	///	<code>// Getting IconId out of an Icon Identifier Class type
	///IconId iconid = new JetBrains.Application.Icons.CompiledIconsCs.CompiledIconCsId(typeof(DodofuxThemedIconsThemedIcons.Trinity));</code>
	///	<code>// Getting image for screen rendering by IconId
	///themediconmanager.Icons[icnoid]</code>
	///	<code>// Getting image for screen rendering by Icon Identifier Class
	///themediconmanager.GetIcon&lt;DodofuxThemedIconsThemedIcons.Trinity&gt;()</code>
	///	<h1>Icons Origin</h1>
	///	<para>This code was generated by a pre-compile build task from a set of input files which are XAML files adhering to a certain convention, as convenient for exporting them from the Illustrator workspace, plus separate PNG files with raster icons. In the projects, these files are designated with <c>ThemedIconsXamlV3</c> and <c>ThemedIconPng</c> build actions and do not themselves get into the output assembly. All of such files are processed, vector images for different themes of the same icon are split and combined into the single list of icons in this assembly. This list is then written into the genearted XAML file (compiled into BAML within assembly), and serves as the source for this generated code.</para>
	///</remarks>
	public sealed class FunctionAppRunMarkersThemedIcons
	{
		#region RunFunctionApp
		
		[global::JetBrains.Application.Icons.CompiledIconsCs.CompiledIconCsAttribute()]
		public sealed class RunFunctionApp : global::JetBrains.Application.Icons.CompiledIconsCs.CompiledIconCsClass
		{

			/// <inheritdoc cref="RunFunctionApp">identifier class</inheritdoc>
			public static global::JetBrains.UI.Icons.IconId Id = new global::JetBrains.Application.Icons.CompiledIconsCs.CompiledIconCsId(typeof(RunFunctionApp));

			/// <summary>Loads the image for Themed Icon RunFunctionApp theme aspect Color.</summary>
			public global::JetBrains.Util.Icons.TiImage Load_Color()
			{
				return global::JetBrains.Util.Icons.TiImageConverter.FromTiSvg(
					"<svg ti:v=\'1\' width=\'16\' height=\'16\' viewBox=\'0,0,16,16\' xmlns=\'http://www.w3.org/2000/svg\' xmlns:ti=\'urn:schemas-jetbrains-com:tisvg\'></svg>");
			}

			/// <summary>Loads the image for Themed Icon RunFunctionApp theme aspect Gray.</summary>
			public global::JetBrains.Util.Icons.TiImage Load_Gray()
			{
				return global::JetBrains.Util.Icons.TiImageConverter.FromTiSvg(
					"<svg ti:v=\'1\' width=\'16\' height=\'16\' viewBox=\'0,0,16,16\' xmlns=\'http://www.w3.org/2000/svg\' xmlns:ti=\'urn:schemas-jetbrains-com:tisvg\'></svg>");
			}

			/// <summary>Loads the image for Themed Icon RunFunctionApp theme aspect GrayDark.</summary>
			public global::JetBrains.Util.Icons.TiImage Load_GrayDark()
			{
				return global::JetBrains.Util.Icons.TiImageConverter.FromTiSvg(
					"<svg ti:v=\'1\' width=\'16\' height=\'16\' viewBox=\'0,0,16,16\' xmlns=\'http://www.w3.org/2000/svg\' xmlns:ti=\'urn:schemas-jetbrains-com:tisvg\'></svg>");
			}

			/// <summary>Returns the set of theme images for Themed Icon RunFunctionApp.</summary>
			public override global::JetBrains.Application.Icons.CompiledIconsCs.CompiledIconCsIdOwner.ThemedIconThemeImage[] GetThemeImages()
			{
				return new global::JetBrains.Application.Icons.CompiledIconsCs.CompiledIconCsIdOwner.ThemedIconThemeImage[] {
						new global::JetBrains.Application.Icons.CompiledIconsCs.CompiledIconCsIdOwner.ThemedIconThemeImage("Color", new global::JetBrains.Application.Icons.CompiledIconsCs.CompiledIconCsIdOwner.LoadImageDelegate(this.Load_Color)),
						new global::JetBrains.Application.Icons.CompiledIconsCs.CompiledIconCsIdOwner.ThemedIconThemeImage("Gray", new global::JetBrains.Application.Icons.CompiledIconsCs.CompiledIconCsIdOwner.LoadImageDelegate(this.Load_Gray)),
						new global::JetBrains.Application.Icons.CompiledIconsCs.CompiledIconCsIdOwner.ThemedIconThemeImage("GrayDark", new global::JetBrains.Application.Icons.CompiledIconsCs.CompiledIconCsIdOwner.LoadImageDelegate(this.Load_GrayDark))};
			}
		}
		
		#endregion

		#region Trigger
		
		[global::JetBrains.Application.Icons.CompiledIconsCs.CompiledIconCsAttribute()]
		public sealed class Trigger : global::JetBrains.Application.Icons.CompiledIconsCs.CompiledIconCsClass
		{

			/// <inheritdoc cref="Trigger">identifier class</inheritdoc>
			public static global::JetBrains.UI.Icons.IconId Id = new global::JetBrains.Application.Icons.CompiledIconsCs.CompiledIconCsId(typeof(Trigger));

			/// <summary>Loads the image for Themed Icon Trigger theme aspect Color.</summary>
			public global::JetBrains.Util.Icons.TiImage Load_Color()
			{
				return global::JetBrains.Util.Icons.TiImageConverter.FromTiSvg(
					"<svg ti:v=\'1\' width=\'16\' height=\'16\' viewBox=\'0,0,16,16\' xmlns=\'http://www.w3.org/2000/svg\' xmlns:ti=\'urn:schemas-jetbrains-com:tisvg\'></svg>");
			}

			/// <summary>Loads the image for Themed Icon Trigger theme aspect Gray.</summary>
			public global::JetBrains.Util.Icons.TiImage Load_Gray()
			{
				return global::JetBrains.Util.Icons.TiImageConverter.FromTiSvg(
					"<svg ti:v=\'1\' width=\'16\' height=\'16\' viewBox=\'0,0,16,16\' xmlns=\'http://www.w3.org/2000/svg\' xmlns:ti=\'urn:schemas-jetbrains-com:tisvg\'></svg>");
			}

			/// <summary>Loads the image for Themed Icon Trigger theme aspect GrayDark.</summary>
			public global::JetBrains.Util.Icons.TiImage Load_GrayDark()
			{
				return global::JetBrains.Util.Icons.TiImageConverter.FromTiSvg(
					"<svg ti:v=\'1\' width=\'16\' height=\'16\' viewBox=\'0,0,16,16\' xmlns=\'http://www.w3.org/2000/svg\' xmlns:ti=\'urn:schemas-jetbrains-com:tisvg\'></svg>");
			}

			/// <summary>Returns the set of theme images for Themed Icon Trigger.</summary>
			public override global::JetBrains.Application.Icons.CompiledIconsCs.CompiledIconCsIdOwner.ThemedIconThemeImage[] GetThemeImages()
			{
				return new global::JetBrains.Application.Icons.CompiledIconsCs.CompiledIconCsIdOwner.ThemedIconThemeImage[] {
						new global::JetBrains.Application.Icons.CompiledIconsCs.CompiledIconCsIdOwner.ThemedIconThemeImage("Color", new global::JetBrains.Application.Icons.CompiledIconsCs.CompiledIconCsIdOwner.LoadImageDelegate(this.Load_Color)),
						new global::JetBrains.Application.Icons.CompiledIconsCs.CompiledIconCsIdOwner.ThemedIconThemeImage("Gray", new global::JetBrains.Application.Icons.CompiledIconsCs.CompiledIconCsIdOwner.LoadImageDelegate(this.Load_Gray)),
						new global::JetBrains.Application.Icons.CompiledIconsCs.CompiledIconCsIdOwner.ThemedIconThemeImage("GrayDark", new global::JetBrains.Application.Icons.CompiledIconsCs.CompiledIconCsIdOwner.LoadImageDelegate(this.Load_GrayDark))};
			}
		}
		
		#endregion
	}
}
