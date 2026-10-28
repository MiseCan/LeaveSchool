<script>
	export default {
		onLaunch: function() {
			console.log('App Launch');
		},
		onShow: function() {
			console.log('App Show');
		},
		onHide: function() {
			console.log('App Hide');
		}
	};
</script>

<style>
	/* 解决头条小程序组件内引入字体不生效的问题 */
	/* #ifdef MP-TOUTIAO */
	@font-face {
		font-family: uniicons;
		src: url('/static/uni.ttf');
	}

	/* #endif */

	/* ===== MD3 expressive tokens（仅 m3-page 页面使用） ===== */
	.m3-page {
		--md-primary: #166B4E;
		--md-on-primary: #FFFFFF;
		--md-primary-container: #A6F2CE;
		--md-on-primary-container: #002116;
		--md-secondary-container: #CBE8DA;
		--md-on-secondary-container: #04211A;
		--md-tertiary: #8B5000;
		--md-tertiary-container: #FFDCC2;
		--md-on-tertiary-container: #2E1500;
		--md-surface: #FAFDF8;
		--md-surface-container-low: #F2F6F0;
		--md-surface-container: #ECF1EA;
		--md-surface-container-high: #E6EBE4;
		--md-on-surface: #171D19;
		--md-on-surface-variant: #404943;
		--md-outline: #707972;
		--md-outline-variant: #C0C9C1;
		--md-error-container: #FFDAD6;
		--md-on-error-container: #410002;
	}
</style>
